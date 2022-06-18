package com.iaas.project1.apptier;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class S3Receiver {
    private final AmazonS3 s3;
    private final AwsProperties awsProperties;

    Logger logger = LoggerFactory.getLogger(S3Receiver.class);

    public S3Receiver(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;

        var builder = AmazonS3Client.builder();
        BuilderUtil.configureBuilder(awsProperties, builder);
        s3 = builder.build();
    }

    public Path getImageFromS3(String imageName) throws IOException {
        S3Object s3Obj = s3.getObject(awsProperties.inputBucket(), imageName);
        S3ObjectInputStream s3InputStream = s3Obj.getObjectContent();
        Path outputPath = Paths.get(imageName);
        long imageSize = Files.copy(s3InputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("got image from s3 {} with size {}",imageName,imageSize);
        return outputPath;
    }

    public void saveOutputToS3(String fileName, String output) throws IOException {
        String name = fileName;
        s3.putObject(awsProperties.outputBucket(), name, output);
    }
}
