package com.iaas.project1.apptier;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ConsoleApp implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(ConsoleApp.class);
    private final SqsReceiver sqsReceiver;
    private final S3Receiver s3Receiver;

    public ConsoleApp(SqsReceiver sqsReceiver, S3Receiver s3Receiver) {

        this.sqsReceiver = sqsReceiver;
        this.s3Receiver = s3Receiver;

    }

    @Override
    public void run(String... args) throws IOException {
        while(true) {
            List<Message> messages = sqsReceiver.receiveRequest();
            for (Message message : messages) {
                logger.info("Message RequestId: {}", message.getMessageAttributes().get("RequestId").getStringValue());
                String messageBody = message.getBody();
                logger.info("Message body: {}", messageBody);

                //JsonNode node = new ObjectMapper().readTree(messageBody);
                //var filename = node.get("filename").asText();
                //var filebytes = node.get("filebytes").binaryValue();

                String output = classify(s3Receiver.getImageFromS3(messageBody));
                // Send response

                sqsReceiver.sendResponse(output, message);
            }
        }
    }

    private String classify(Path path) throws IOException {
        //Path path = Paths.get("/tmp/"+filename);
        //Files.write(path, filebytes);

        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("python3 image_classification.py "+path);

        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = null;

        try {
            line = input.readLine();
            logger.info("Executed py got output: {}", line);
            return line.split(",")[1];
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
