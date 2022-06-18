package com.iaas.project1.webtier;

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
    final S3Repository s3Repository;
    private final Ec2Monitor ec2Monitor;
    public ClassifyController(S3Repository s3Repository, SqsSender sqsSender, Ec2Monitor ec2Monitor) {
        this.s3Repository = s3Repository;
        this.sqsSender = sqsSender;
        this.ec2Monitor = ec2Monitor;
    }

    @RequestMapping("/")
    public String classify(@RequestParam("myfile") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        s3Repository.saveImageToS3(file.getOriginalFilename(), file);
        var output = sqsSender.sendRequest(file.getOriginalFilename());
        return output;
    }
}
