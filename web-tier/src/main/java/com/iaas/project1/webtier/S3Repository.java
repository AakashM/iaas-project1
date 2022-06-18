package com.iaas.project1.webtier;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Repository
public class S3Repository {
    private static final Logger logger = LoggerFactory.getLogger(S3Repository.class);

    private final AmazonS3 s3;
    private final AwsProperties awsProperties;

    //create SQS Client instance
    public S3Repository(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;

        var builder = AmazonS3Client.builder();
        BuilderUtil.configureBuilder(awsProperties, builder);
        s3 = builder.build();

        logger.info("S3Repository created");
    }

    public void saveImageToS3(String fileName, MultipartFile uploadedImageFile) throws IOException {
        logger.info("Received file: {}", fileName);
        String name = fileName;
        InputStream is = uploadedImageFile.getInputStream();
        s3.putObject(awsProperties.inputBucket(), name, is, null);
    }
}
