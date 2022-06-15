package com.iaas.project1.webtier;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class SqsSender {
    private static final Logger logger = LoggerFactory.getLogger(SqsSender.class);
    
    private final AmazonSQS sqs;
    private final AwsProperties awsProperties;

    //create SQS Client instance
    public SqsSender(AwsProperties awsProperties){
        this.awsProperties = awsProperties;
        BasicAWSCredentials AWS_CREDENTIALS = GeneralSettings.getAWSCREDENTIALS();

        sqs = AmazonSQSClient.builder()
                .withCredentials(new AWSStaticCredentialsProvider(AWS_CREDENTIALS))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsProperties.endpoint(), awsProperties.region()))
                .build();

        logger.info("SQSSender created");
    }

    public String sendRequest(String message){
        var requestId = java.util.UUID.randomUUID().toString();

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("RequestId", new MessageAttributeValue().withDataType("String").withStringValue(requestId));

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(awsProperties.requestQueueUrl())
                .withMessageBody(message)
                .withMessageAttributes(messageAttributes);
        sqs.sendMessage(send_msg_request);

        logger.info("message sent to the request queue: {}",message);
        return "Unknown";
    }

}
