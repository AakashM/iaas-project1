package com.iaas.project1.webtier;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class Ec2Monitor {

    private static final Logger logger = LoggerFactory.getLogger(Ec2Monitor.class);
    private final Thread pollThread;
//    int oldRequestsSize = 0;
    private final SqsSender sqsSender;
    private final AwsProperties awsProperties;
    private final AmazonEC2 ec2;

    private int instanceNum = 1;

//    private String instanceId;


    public Ec2Monitor(SqsSender sqsSender, AwsProperties awsProperties) {
        this.sqsSender = sqsSender;
        this.awsProperties = awsProperties;

        var builder = AmazonEC2Client.builder();
        BuilderUtil.configureBuilder(awsProperties, builder);
        ec2 = builder.build();

        this.pollThread = new Thread(this::startMonitoringSqsQueueSize);
        this.pollThread.start();

        logger.info("Ec2Monitor created");
    }

    private void startMonitoringSqsQueueSize() {
        while(true) {
            // Get size from sqs queue
            try {
                var queueSize = sqsSender.getSqsQueueSize(); // this size is fetched from queue
                int numOfRunningInstances = getCountOfRunningInstances();
//                if(oldRequestsSize == 0 && queueSize > 0) {
                    int numOfInstancesToCreate = Math.min(queueSize, 15) - numOfRunningInstances;
                    if(numOfInstancesToCreate > 0)
                        createInstances(numOfInstancesToCreate);
//                } //else if(oldRequestsSize > 1 && queueSize == 0) {
//                    terminateInstance(instanceId);
//                }
//                oldRequestsSize = queueSize;

                Thread.sleep(TimeUnit.SECONDS.toSeconds(2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createInstances(int numOfInstances) {
        for(int i=0;i<numOfInstances;i++) {
            String instanceId = createOneInstance("app-tier-"+instanceNum);

            //generate id between 1 and 19
            instanceNum++;

            if(instanceId == null)
                logger.error("Failed to create instance");
            else
                logger.info("Created instance " + instanceId);
        }
    }

    private String createOneInstance(String instanceName) {
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

//    private void terminateInstance(String instanceId) {
//        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
//                .withInstanceIds(instanceId);
//        ec2.terminateInstances(terminateInstancesRequest)
//                .getTerminatingInstances()
//                .get(0)
//                .getPreviousState()
//                .getName();
//    }

    private int getCountOfRunningInstances() {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

        Filter runningInstancesFilter = new Filter();
        runningInstancesFilter.setName("instance-state-name");
        runningInstancesFilter.setValues(Arrays.asList(new String[] {"running", "pending"}));

        describeInstancesRequest.setFilters( Arrays.asList(new Filter[] {runningInstancesFilter}));

        describeInstancesRequest.setMaxResults(1000);

        DescribeInstancesResult result = ec2.describeInstances(describeInstancesRequest);

        int count = 0;
        for(Reservation r : result.getReservations()) {
            //count += r.getInstances().size();
            for (Instance instance : r.getInstances()) {
                if (instance.getTags().get(0).getValue().contains("app-tier")) {
                    count++;
                }
            }
        }

        return count;
    }
}
