package com.iaas.project1.apptier;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ConsoleApp implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(ConsoleApp.class);
    private final SqsReceiver sqsReceiver;

    public ConsoleApp(SqsReceiver sqsReceiver) {
        this.sqsReceiver = sqsReceiver;
    }

    @Override
    public void run(String... args) throws IOException {
        while(true) {
            List<Message> messages = sqsReceiver.receiveRequest();
            for (Message message : messages) {
                logger.info("Message RequestId: {}", message.getMessageAttributes().get("RequestId").getStringValue());
                String messageBody = message.getBody();
                logger.info("Message body: {}", messageBody);

                JsonNode node = new ObjectMapper().readTree(messageBody);
                var filename = node.get("filename").asText();
                var filebytes = node.get("filebytes").binaryValue();

                String output = classify(filename, filebytes);
                // Send response

                sqsReceiver.sendResponse(output, message);
            }
        }
    }

    private String classify(String filename, byte[] filebytes) {
        return "Unknown";
    }
}
