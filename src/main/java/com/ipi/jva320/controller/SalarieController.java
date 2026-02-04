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
@RequestMapping("/salaries")
public class SalarieController {

    @Autowired
    private SalarieAideADomicileService salarieAideADomicileService;

    // =======================
    // LISTE DES SALARIÉS
    // =======================
    @GetMapping
    public String listSalaries(ModelMap model,
                               @RequestParam(value = "nom", required = false) String nom) {

        if (nom != null && !nom.isBlank()) {
            List<SalarieAideADomicile> salaries = salarieAideADomicileService.getSalaries(nom);
            if (salaries.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucun salarié trouvé avec le nom : " + nom);
            }
            model.put("salaries", salaries); // passer la liste à la vue
            model.put("nbSalaries", salaries.size());
            return "list";
        }

        List<SalarieAideADomicile> salaries = salarieAideADomicileService.getSalaries();
        model.put("salaries", salaries);
        model.put("nbSalaries", salaries.size());
        return "list";
    }

    // =======================
    // DÉTAIL SALARIÉ
    // =======================
    @GetMapping("/{id}")
    public String getDetailSalarie(@PathVariable Long id, ModelMap model) {
        SalarieAideADomicile salarie = salarieAideADomicileService.getSalarie(id);
        if (salarie == null) {
            return "redirect:/salaries";
        }
        model.put("salarie", salarie);
        return "detail_Salarie";
    }

    // =======================
    // FORMULAIRE CRÉATION
    // =======================
    @GetMapping("/aide/new")
    public String newSalarie(ModelMap model) {
        model.put("salarie", new SalarieAideADomicile());
        return "new_Salarie";
    }

    // =======================
    // SAUVEGARDE
    // =======================
    @PostMapping("/aide/save")
    public String addSalarie(@ModelAttribute SalarieAideADomicile salarie)
            throws SalarieException, EntityExistsException {
        salarieAideADomicileService.creerSalarieAideADomicile(salarie);
        return "redirect:/salaries/" + salarie.getId();
    }

    // =======================
    // SUPPRESSION
    // =======================
    @GetMapping("/{id}/delete")
    public String deleteSalarie(@PathVariable Long id)
            throws SalarieException, EntityExistsException {
        salarieAideADomicileService.deleteSalarieAideADomicile(id);
        return "redirect:/salaries";
    }

    // =======================
    // MISE À JOUR
    // =======================
    @PostMapping("/{id}")
    public String updateSalarie(@PathVariable Long id,
                                @ModelAttribute SalarieAideADomicile updatedSalarie)
            throws SalarieException, EntityExistsException {
        salarieAideADomicileService.updateSalarieAideADomicile(updatedSalarie);
        return "redirect:/salaries/" + updatedSalarie.getId();
    }
}
