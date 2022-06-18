package com.iaas.project1.webtier;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

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

    //to upload single image
    public void saveImageToS3(String fileName, MultipartFile uploadedImageFile) throws IOException {
        String name = fileName;
        InputStream is = uploadedImageFile.getInputStream();
        s3.putObject(awsProperties.inputBucket(), name, is, null);
    }

    /*
     * Save the input images in S3.
     * */
    public List<String> saveMultipleImagesToS3(MultipartFile[] files) throws IOException  {

        List<String> imageKeyList = new ArrayList<String>();

        for (MultipartFile uploadedFile : files) {
            logger.info("Received file: {}", uploadedFile.getOriginalFilename());
            saveImageToS3(uploadedFile.getOriginalFilename(), uploadedFile);
            imageKeyList.add(uploadedFile.getOriginalFilename());
        }
        return imageKeyList;
    }

}
