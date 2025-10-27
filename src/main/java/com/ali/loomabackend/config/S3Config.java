package com.ali.loomabackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.net.URI; // <-- Make sure to import this

@Configuration
public class S3Config {
    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    // This is fine for local development
    private boolean useIamRole = false;

    @Bean
    public S3Client s3Client() {
        // This is the correct endpoint for DigitalOcean
        String endpointUrl = "https://" + region + ".digitaloceanspaces.com";

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .endpointOverride(URI.create(endpointUrl)) // <-- REQUIRED FIX
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        // The presigner ALSO needs to know the correct endpoint
        String endpointUrl = "https://" + region + ".digitaloceanspaces.com";

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .endpointOverride(URI.create(endpointUrl)) // <-- REQUIRED FIX
                .build();
    }

    private AwsCredentialsProvider awsCredentialsProvider() {
        if (useIamRole) {
            // Use IAM role (recommended for production on EC2/ECS)
            return DefaultCredentialsProvider.create();
        } else {
            // Use explicit credentials (for local development)
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            return StaticCredentialsProvider.create(credentials);
        }
    }
}