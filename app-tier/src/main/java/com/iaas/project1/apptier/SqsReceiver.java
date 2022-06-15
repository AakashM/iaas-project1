package com.iaas.project1.apptier;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SqsReceiver {
    private final AmazonSQS sqs;
    private final AwsProperties awsProperties;

    public SqsReceiver(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;

        var builder = AmazonSQSClient.builder();
        BuilderUtil.configureBuilder(awsProperties, builder);
        sqs = builder.build();
    }

    public List<Message> receiveRequest() {
        var receive_msg_request = new ReceiveMessageRequest()
                .withQueueUrl(awsProperties.requestQueueUrl())
                .withWaitTimeSeconds(20)
                .withMessageAttributeNames("RequestId");

        return sqs.receiveMessage(receive_msg_request).getMessages();
    }

    public void sendResponse(String response, Message message) {
        // Send response then delete

        sqs.deleteMessage(awsProperties.requestQueueUrl(), message.getReceiptHandle());
    }
}