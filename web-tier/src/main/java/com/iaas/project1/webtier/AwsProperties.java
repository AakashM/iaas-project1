package com.iaas.project1.webtier;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        String endpoint,
        String region,

        String requestQueueUrl,
        String responseQueueUrl,

        String inputBucket,
        String outputBucket,

        String accessKey,
        String secretKey
) {
    public AWSCredentials getAwsCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
