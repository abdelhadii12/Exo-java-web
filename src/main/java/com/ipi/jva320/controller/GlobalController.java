package com.ipi.jva320.controller;

import com.ipi.jva320.service.SalarieAideADomicileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {

    @Autowired
    private SalarieAideADomicileService salarieService;

    @ModelAttribute("nbSalaries")
    public long nbSalaries() {
        return salarieService.countSalaries();
    }
}
