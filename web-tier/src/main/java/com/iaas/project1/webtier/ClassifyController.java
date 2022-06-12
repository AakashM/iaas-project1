package com.iaas.project1.webtier;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClassifyController {

    @RequestMapping("/")
    public String classify() {
        System.out.println("Received message");
        return "Hello World from Spring Boot";
    }
}
