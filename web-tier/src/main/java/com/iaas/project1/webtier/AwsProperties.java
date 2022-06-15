package com.iaas.project1.webtier;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
        String endpoint,
        String region,

        String requestQueueUrl,
        String responseQueueUrl
) {
}
