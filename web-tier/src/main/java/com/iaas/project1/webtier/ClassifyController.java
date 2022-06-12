package com.iaas.project1.webtier;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ClassifyController {

    @RequestMapping("/")
    public String classify(@RequestParam("myfile") MultipartFile file) {
        System.out.println("Received file: " + file.getOriginalFilename());
        return "Unknown";
    }
}
