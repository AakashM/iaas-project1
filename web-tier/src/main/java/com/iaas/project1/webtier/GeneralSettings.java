package com.iaas.project1.webtier;

import com.amazonaws.auth.BasicAWSCredentials;

public class GeneralSettings {
    public static final String SQS_REQUEST_URL = "https://sqs.us-east-1.amazonaws.com/124940588792/CSE546_Project1_Request";
    public static final String SQS_RESPONSE_URL = "https://sqs.us-east-1.amazonaws.com/124940588792/CSE546_Project1_Response";

    public static final  String S3_InputBucket="cse546-project1-input";
    public static final  String S3_OutputBucket="cse546-project1-output";

    public static final String AMI_ID = "ami-0bb1040fdb5a076bc";
    public static final String KEYPAIR_NAME = "debeshaws";
    public static final String SECURITY_GROUP_ID = "sg-024a97e0e7db9abba";

    private static final String ACCESS_KEY = "AKIAR2FYKC34HDRIALXK";
    private static final String SECRET_KEY = "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";

    public static final BasicAWSCredentials getAWSCREDENTIALS() {
        BasicAWSCredentials AWS_CREDENTIALS = new BasicAWSCredentials(
                ACCESS_KEY,
                SECRET_KEY
        );
        return AWS_CREDENTIALS;
    }
}
