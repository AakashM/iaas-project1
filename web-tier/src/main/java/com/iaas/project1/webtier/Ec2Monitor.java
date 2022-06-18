package com.iaas.project1.webtier;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class Ec2Monitor {
    private final Thread pollThread;
    int oldRequestsSize = 0;
    private final SqsSender sqsSender;
    private final AwsProperties awsProperties;
    private final AmazonEC2 ec2;

    private int instanceNum = 1;

    private String instanceId;


    public Ec2Monitor(SqsSender sqsSender, AwsProperties awsProperties) {
        this.sqsSender = sqsSender;
        this.awsProperties = awsProperties;

        var builder = AmazonEC2Client.builder();
        BuilderUtil.configureBuilder(awsProperties, builder);
        ec2 = builder.build();

        this.pollThread = new Thread(this::startMonitoringSqsQueueSize);
        this.pollThread.start();
    }

    private void startMonitoringSqsQueueSize() {
        while(true) {
            // Get size from sqs queue
            try {
                var newRequestsSize = sqsSender.getSqsQueueSize(); // this size is fetched from queue
                if(oldRequestsSize == 0 && newRequestsSize > 0) {
                    instanceId = createInstance("app-instance-"+String.valueOf(instanceNum));
                    instanceNum = (instanceNum+1)%19 + 1;
                } else if(oldRequestsSize > 1 && newRequestsSize == 0) {
                    terminateInstance(instanceId);
                }
                oldRequestsSize = newRequestsSize;

                Thread.sleep(TimeUnit.SECONDS.toSeconds(2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String createInstance(String instanceName) {
        // Launch an Amazon EC2 Instance
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(GeneralSettings.AMI_ID)
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(GeneralSettings.KEYPAIR_NAME)
                .withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Terminate)
                .withSecurityGroupIds(GeneralSettings.SECURITY_GROUP_ID);

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

        Instance instance = runInstancesResult.getReservation().getInstances().get(0);
        String instanceId = instance.getInstanceId();

        // Setting up the tags for the instance
        CreateTagsRequest createTagsRequest = new CreateTagsRequest()
                .withResources(instance.getInstanceId())
                .withTags(new Tag("Name", instanceName));
        ec2.createTags(createTagsRequest);

        // Starting the Instance
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceId);

        ec2.startInstances(startInstancesRequest);

        return instanceId;
    }

    private void terminateInstance(String instanceId) {
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                .withInstanceIds(instanceId);
        ec2.terminateInstances(terminateInstancesRequest)
                .getTerminatingInstances()
                .get(0)
                .getPreviousState()
                .getName();
    }
}
