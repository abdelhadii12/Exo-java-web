package com.ipi.jva320.controller;

import com.ipi.jva320.exception.SalarieException;
import com.ipi.jva320.model.SalarieAideADomicile;
import com.ipi.jva320.service.SalarieAideADomicileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SalarieController {

    @Autowired
    private SalarieAideADomicileService salarieService;

    @GetMapping("/salaries/{id}")
    public String afficherSalarie(ModelMap model, @PathVariable Long id) {

        SalarieAideADomicile salarie = salarieService.getSalarie(id);

        if (salarie == null) {
            // salarié inexistant
            return "error/404";
        }

        model.put("infoSalarie", salarie);
        return "detail_Salarie";
    }

    @GetMapping("/salaries/aide/new")
    public String nouveauSalarie() {

        return "add_salarie";
    }

    @PostMapping("/salaries/save")
    public String creeSalarie(SalarieAideADomicile salarie){
        try {
            salarieService.creerSalarieAideADomicile(salarie);
        } catch (SalarieException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}
