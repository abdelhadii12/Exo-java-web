package com.ipi.jva320.controller;

import com.ipi.jva320.exception.SalarieException;
import com.ipi.jva320.model.SalarieAideADomicile;
import com.ipi.jva320.service.SalarieAideADomicileService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class SalarieController {

    @Autowired
    private SalarieAideADomicileService salarieAideADomicileService;

    // =======================
    // DÉTAIL SALARIÉ
    // =======================
    @GetMapping("/salaries/{id}")
    public String getDetailSalarie(@PathVariable Long id, ModelMap model) {
        SalarieAideADomicile salarie = salarieAideADomicileService.getSalarie(id);

        if (salarie == null) {
            return "redirect:/salaries";
        }

        model.put("salarie", salarie);
        return "detail_Salarie";
    }

    // =======================
    // RECHERCHE PAR NOM
    // =======================
    @GetMapping("/salaries")
    public String searchSalarie(
            @RequestParam(value = "nom", required = false) String nom,
            ModelMap model) {

        if (nom != null && !nom.isBlank()) {
            List<SalarieAideADomicile> salaries =
                    salarieAideADomicileService.getSalaries(nom);

            if (salaries.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aucun salarié trouvé avec le nom : " + nom
                );
            }

            model.put("salarie", salaries.get(0));
            return "detail_Salarie";
        }

        return "redirect:/";
    }

    // =======================
    // FORMULAIRE CRÉATION
    // =======================
    @GetMapping("/salaries/aide/new")
    public String newSalarie(ModelMap model) {
        model.put("salarie", new SalarieAideADomicile());
        return "new_Salarie";
    }

    // =======================
    // SAUVEGARDE
    // =======================
    @PostMapping("/salaries/aide/save")
    public String addSalarie(@ModelAttribute SalarieAideADomicile salarie)
            throws SalarieException, EntityExistsException {

        salarieAideADomicileService.creerSalarieAideADomicile(salarie);
        return "redirect:/salaries/" + salarie.getId();
    }

    // =======================
    // SUPPRESSION
    // =======================
    @GetMapping("/salaries/{id}/delete")
    public String deleteSalarie(@PathVariable Long id)
            throws SalarieException, EntityExistsException {

        salarieAideADomicileService.deleteSalarieAideADomicile(id);
        return "redirect:/salaries";
    }

    // =======================
    // MISE À JOUR
    // =======================
    @PostMapping("/salaries/{id}")
    public String updateSalarie(
            @PathVariable Long id,
            @ModelAttribute SalarieAideADomicile updatedSalarie)
            throws SalarieException, EntityExistsException {

        salarieAideADomicileService.updateSalarieAideADomicile(updatedSalarie);
        return "redirect:/salaries/" + updatedSalarie.getId();
    }
}
