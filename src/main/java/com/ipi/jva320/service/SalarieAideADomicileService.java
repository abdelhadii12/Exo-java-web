package com.ipi.jva320.service;

import com.ipi.jva320.exception.SalarieException;
import com.ipi.jva320.model.Entreprise;
import com.ipi.jva320.model.SalarieAideADomicile;
import com.ipi.jva320.repository.SalarieAideADomicileRepository;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SalarieAideADomicileService {

    @Autowired
    private SalarieAideADomicileRepository salarieAideADomicileRepository;

    public Long countSalaries() {
        return salarieAideADomicileRepository.count();
    }

    public List<SalarieAideADomicile> getSalaries() {
        return StreamSupport.stream(
                salarieAideADomicileRepository.findAll().spliterator(),
                false
        ).collect(Collectors.toList());
    }

    public List<SalarieAideADomicile> getSalaries(String nom) {
        return salarieAideADomicileRepository.findAllByNom(nom, null);
    }

    public List<SalarieAideADomicile> getSalaries(String nom, Pageable pageable) {
        return salarieAideADomicileRepository.findAllByNom(nom, pageable);
    }

    public Page<SalarieAideADomicile> getSalaries(Pageable pageable) {
        return salarieAideADomicileRepository.findAll(pageable);
    }

    public SalarieAideADomicile getSalarie(Long id) {
        return salarieAideADomicileRepository
                .findById(id)
                .orElse(null);
    }

    public SalarieAideADomicile creerSalarieAideADomicile(
            SalarieAideADomicile salarieAideADomicile)
            throws SalarieException, EntityExistsException {

        if (salarieAideADomicile.getId() != null) {
            throw new SalarieException("L'id ne doit pas être fourni");
        }

        return salarieAideADomicileRepository.save(salarieAideADomicile);
    }

    public SalarieAideADomicile updateSalarieAideADomicile(
            SalarieAideADomicile salarieAideADomicile)
            throws SalarieException, EntityExistsException {

        if (salarieAideADomicile.getId() == null) {
            throw new SalarieException("L'id doit être fourni");
        }

        if (!salarieAideADomicileRepository.existsById(salarieAideADomicile.getId())) {
            throw new SalarieException(
                    "Le salarié n'existe pas (id=" + salarieAideADomicile.getId() + ")"
            );
        }

        return salarieAideADomicileRepository.save(salarieAideADomicile);
    }

    public void deleteSalarieAideADomicile(Long id)
            throws SalarieException, EntityExistsException {

        if (id == null) {
            throw new SalarieException("L'id doit être fourni");
        }

        if (!salarieAideADomicileRepository.existsById(id)) {
            throw new SalarieException("Le salarié n'existe pas (id=" + id + ")");
        }

        salarieAideADomicileRepository.deleteById(id);
    }

    public long calculeLimiteEntrepriseCongesPermis(
            LocalDate moisEnCours,
            double congesPayesAcquisAnneeNMoins1,
            LocalDate moisDebutContrat,
            LocalDate premierJourDeConge,
            LocalDate dernierJourDeConge) {

        double proportionPonderee = Math.max(
                Entreprise.proportionPondereeDuMois(premierJourDeConge),
                Entreprise.proportionPondereeDuMois(dernierJourDeConge)
        );

        double limite = proportionPonderee * congesPayesAcquisAnneeNMoins1;

        Double moyenneConges =
                salarieAideADomicileRepository.partCongesPrisTotauxAnneeNMoins1();

        double proportionMois =
                ((premierJourDeConge.getMonthValue()
                        - Entreprise.getPremierJourAnneeDeConges(moisEnCours).getMonthValue()) % 12) / 12d;

        limite += (proportionMois - moyenneConges) * 0.2 * congesPayesAcquisAnneeNMoins1;

        int distanceMois =
                (dernierJourDeConge.getMonthValue() - moisEnCours.getMonthValue()) % 12;

        limite += limite * 0.1 * distanceMois / 12;

        int anciennete = moisEnCours.getYear() - moisDebutContrat.getYear();
        limite += Math.min(anciennete, 10);

        return BigDecimal.valueOf(limite)
                .setScale(3, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    public void ajouteConge(
            SalarieAideADomicile salarie,
            LocalDate jourDebut,
            LocalDate jourFin)
            throws SalarieException {

        if (!salarie.aLegalementDroitADesCongesPayes()) {
            throw new SalarieException("Pas de droit légal aux congés payés");
        }

        LinkedHashSet<LocalDate> jours =
                salarie.calculeJoursDeCongeDecomptesPourPlage(jourDebut, jourFin);

        if (jours.isEmpty()) {
            throw new SalarieException("Aucun congé à décompter");
        }

        if (jours.iterator().next().isBefore(salarie.getMoisEnCours())) {
            throw new SalarieException("Congé avant le mois en cours interdit");
        }

        LinkedHashSet<LocalDate> anneeN = jours.stream()
                .filter(d -> !d.isAfter(
                        LocalDate.of(
                                Entreprise.getPremierJourAnneeDeConges(
                                        salarie.getMoisEnCours()).getYear() + 1,
                                5, 31)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int nb = anneeN.size();

        if (jours.size() > nb + 1) {
            throw new SalarieException("Congé dans l'année suivante interdit");
        }

        if (nb > salarie.getCongesPayesRestantAnneeNMoins1()) {
            throw new SalarieException("Dépassement des congés acquis");
        }

        double limiteEntreprise = calculeLimiteEntrepriseCongesPermis(
                salarie.getMoisEnCours(),
                salarie.getCongesPayesAcquisAnneeNMoins1(),
                salarie.getMoisDebutContrat(),
                jourDebut,
                jourFin
        );

        if (nb < limiteEntreprise) {
            throw new SalarieException("Dépassement de la limite entreprise");
        }

        salarie.getCongesPayesPris().addAll(jours);
        salarie.setCongesPayesPrisAnneeNMoins1(nb);

        salarieAideADomicileRepository.save(salarie);
    }

    public void clotureMois(
            SalarieAideADomicile salarie,
            double joursTravailles)
            throws SalarieException {

        salarie.setJoursTravaillesAnneeN(
                salarie.getJoursTravaillesAnneeN() + joursTravailles
        );

        salarie.setCongesPayesAcquisAnneeN(
                salarie.getCongesPayesAcquisAnneeN()
                        + salarie.CONGES_PAYES_ACQUIS_PAR_MOIS
        );

        salarie.setMoisEnCours(salarie.getMoisEnCours().plusMonths(1));

        if (salarie.getMoisEnCours().getMonthValue() == 6) {
            clotureAnnee(salarie);
        }

        salarieAideADomicileRepository.save(salarie);
    }

    void clotureAnnee(SalarieAideADomicile salarie) {
        salarie.setJoursTravaillesAnneeNMoins1(
                salarie.getJoursTravaillesAnneeN()
        );
        salarie.setCongesPayesAcquisAnneeNMoins1(
                salarie.getCongesPayesAcquisAnneeN()
        );
        salarie.setCongesPayesPrisAnneeNMoins1(0);
        salarie.setJoursTravaillesAnneeN(0);
        salarie.setCongesPayesAcquisAnneeN(0);

        salarie.setCongesPayesPris(
                salarie.getCongesPayesPris().stream()
                        .filter(d -> d.isAfter(
                                LocalDate.of(
                                        Entreprise.getPremierJourAnneeDeConges(
                                                salarie.getMoisEnCours()).getYear(),
                                        5, 31)))
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );

        salarieAideADomicileRepository.save(salarie);
    }
}
