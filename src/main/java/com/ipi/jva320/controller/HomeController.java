package com.ipi.jva320.controller;
import com.ipi.jva320.model.SalarieAideADomicile;
import com.ipi.jva320.service.SalarieAideADomicileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private SalarieAideADomicileService salarieAideADomicileService;

    @GetMapping("/")
    public String getHomePage(ModelMap model) {
        salarieAideADomicileService.countSalaries();
        Long nbSalaries = salarieAideADomicileService.countSalaries();
        model.put("nbSalaries", nbSalaries);
        return "home";
    }}
