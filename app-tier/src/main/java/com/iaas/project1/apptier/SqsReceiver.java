package com.iaas.project1.apptier;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        var requestId = message.getMessageAttributes().get("RequestId").getStringValue();

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("RequestId", new MessageAttributeValue().withDataType("String").withStringValue(requestId));

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(awsProperties.responseQueueUrl())
                .withMessageBody(response)
                .withMessageAttributes(messageAttributes);
        sqs.sendMessage(send_msg_request);

        // Send response then delete
        sqs.deleteMessage(awsProperties.requestQueueUrl(), message.getReceiptHandle());
    }
}
