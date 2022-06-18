package com.iaas.project1.webtier;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Ec2Monitor {
    private final Thread pollThread;
    int oldRequestsSize = 0;

    public Ec2Monitor() {
        this.pollThread = new Thread(this::startMonitoringSqsQueueSize);
        this.pollThread.start();
    }

    private void startMonitoringSqsQueueSize() {
        while(true) {
            // Get size from sqs queue
            try {
                var newRequestsSize = getSqsQueueSize(); // this size is fetched from queue
                if(oldRequestsSize == 0 && newRequestsSize > 0) {
                    startInstance();
                } else if(oldRequestsSize > 1 && newRequestsSize == 0) {
                    stopInstance();
                }
                oldRequestsSize = newRequestsSize;

                Thread.sleep(TimeUnit.SECONDS.toSeconds(2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int getSqsQueueSize() {
        return 0; // use the sqs sdk to get the queue size from aws
    }

    private void startInstance() {

    }

    private void stopInstance() {

    }
}
