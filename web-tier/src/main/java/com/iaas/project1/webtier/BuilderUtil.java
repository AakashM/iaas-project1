package com.iaas.project1.webtier;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;

public class BuilderUtil {
    public static void configureBuilder(AwsProperties awsProperties, AwsClientBuilder builder) {
        builder.withCredentials(new AWSStaticCredentialsProvider(awsProperties.getAwsCredentials()));

        if (awsProperties.endpoint() != null) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsProperties.endpoint(), awsProperties.region()));
        } else {
            builder.withRegion(awsProperties.region());
        }
    }
}
