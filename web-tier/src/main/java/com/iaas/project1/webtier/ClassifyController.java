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
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class ClassifyController {

    Logger logger = LoggerFactory.getLogger(ClassifyController.class);

    final SqsSender sqsSender;
    final S3Repository s3Repository;
    public ClassifyController(S3Repository s3Repository, SqsSender sqsSender) {
        this.s3Repository = s3Repository;
        this.sqsSender = sqsSender;
    }

    @RequestMapping("/")
    public List<String> classify(@RequestParam("myfile") MultipartFile[] files) throws IOException, ExecutionException, InterruptedException {

        List<String> s3ImagesList= s3Repository.saveMultipleImagesToS3(files);
        //ObjectNode object = new ObjectMapper().createObjectNode();
        //object.put("filename", file.getOriginalFilename());
        //object.put("filebytes", file.getBytes());

        //s3Repository.saveImageToS3(file.getOriginalFilename(), file);
        for (String imageName : s3ImagesList) {
            var output = sqsSender.sendRequest(imageName);
        }
        //return output;
        return s3ImagesList;
    }
}
