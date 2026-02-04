package com.ipi.jva320.service;

import com.ipi.jva320.exception.SalarieException;
import com.ipi.jva320.model.SalarieAideADomicile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitService implements CommandLineRunner {

    @Autowired
    private SalarieAideADomicileService salarieAideADomicileService;

    @Override
    public void run(String... args) {
        try {
            if (salarieAideADomicileService.countSalaries() > 0) {
                return;
            }

            LocalDate date = LocalDate.of(2022, 12, 5);

            salarieAideADomicileService.creerSalarieAideADomicile(
                    new SalarieAideADomicile(
                            "Jean",
                            date,
                            date,
                            20,
                            0,
                            80,
                            10,
                            1
                    )
            );

            salarieAideADomicileService.creerSalarieAideADomicile(
                    new SalarieAideADomicile(
                            "Abdelhadi",
                            date,
                            date,
                            20,
                            0,
                            80,
                            10,
                            1
                    )
            );

        } catch (SalarieException e) {
            e.printStackTrace();
        }
    }
}
