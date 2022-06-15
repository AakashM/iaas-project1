package com.iaas.project1.webtier;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQSRepository {

    private static final String requestQueueURL = GeneralSettings.SQS_REQUEST_URL;
    private static final String responseQueueURL = GeneralSettings.SQS_RESPONSE_URL;

    private static final Logger logger = LoggerFactory.getLogger(SQSRepository.class);
    
    private AmazonSQS sqsClient;

    //create SQS Client instance
    public SQSRepository(){
        BasicAWSCredentials AWS_CREDENTIALS = GeneralSettings.getAWSCREDENTIALS();

        sqsClient = AmazonSQSClient.builder()
                .withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS))
                .withRegion(Regions.US_EAST_1)
                .build();

        logger.info("SQSRepository created");
    }

    public void sendMsgToRequestQueue(String message){
        sqsClient.sendMessage(requestQueueURL, message);
        logger.info("message sent to the request queue ",message);
    }

}
