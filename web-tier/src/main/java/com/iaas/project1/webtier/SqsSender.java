package com.iaas.project1.webtier;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Repository
public class SqsSender {
    private static final Logger logger = LoggerFactory.getLogger(SqsSender.class);

    private final AmazonSQS sqs;
    private final AwsProperties awsProperties;
    private final Map<String, CompletableFuture<String>> pendingRequests;
    private final Thread pollThread;

    //create SQS Client instance
    public SqsSender(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        this.pendingRequests = new ConcurrentHashMap<>();

        var builder = AmazonSQSClient.builder();
        BuilderUtil.configureBuilder(awsProperties, builder);
        sqs = builder.build();

        this.pollThread = new Thread(this::startPollingResponses);
        this.pollThread.start();

        logger.info("SQSSender created");
    }

    private void startPollingResponses() {
        var receive_msg_request = new ReceiveMessageRequest()
                .withQueueUrl(awsProperties.responseQueueUrl())
                .withWaitTimeSeconds(20)
                .withMessageAttributeNames("RequestId");

        while(true) {
            for (Message message : sqs.receiveMessage(receive_msg_request).getMessages()) {
                var requestId = message.getMessageAttributes().get("RequestId").getStringValue();
                var future = pendingRequests.get(requestId);

                logger.info("Received response: {}", message.getBody());
                if(future == null) {
                    logger.warn("Got no pending request for {}" + requestId);
                    continue;
                }

                future.complete(message.getBody());
                sqs.deleteMessage(awsProperties.responseQueueUrl(), message.getReceiptHandle());
            }
        }
    }

    public String sendRequest(String message) throws ExecutionException, InterruptedException {
        var requestId = java.util.UUID.randomUUID().toString();

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("RequestId", new MessageAttributeValue().withDataType("String").withStringValue(requestId));

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(awsProperties.requestQueueUrl())
                .withMessageBody(message)
                .withMessageAttributes(messageAttributes);
        sqs.sendMessage(send_msg_request);

        var future = new CompletableFuture<String>();
        pendingRequests.put(requestId, future);

//        logger.info("message sent to the request queue: {}", message);
        return future.get();
    }

}
