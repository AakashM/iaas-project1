package com.iaas.project1.webtier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class ClassifyController {

    Logger logger = LoggerFactory.getLogger(ClassifyController.class);

    final SqsSender sqsSender;

    public ClassifyController(SqsSender sqsSender) {
        this.sqsSender = sqsSender;
    }

    @RequestMapping("/")
    public String classify(@RequestParam("myfile") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        logger.info("Received file: {}", file.getOriginalFilename());

        ObjectNode object = new ObjectMapper().createObjectNode();
        object.put("filename", file.getOriginalFilename());
        object.put("filebytes", file.getBytes());

        var output = sqsSender.sendRequest(object.toString());
        return output;
    }
}
