package com.ashconversion.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test-api")
    public String test() {
        return "L'application Spring Boot fonctionne parfaitement ! Le probleme vient donc de la configuration JSP ou du chemin des vues.";
    }
}