/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compress;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.config.Awsproxyconfig;
import com.tinify.Options;
import com.tinify.Tinify;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

/**
 *
 * @author Karthik Suresh
 */
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value = "file:application.properties", ignoreResourceNotFound = true)
})
public class ImageCompression {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCompression.class);

    public static void main(String a[]) {
        LOGGER.info("Inside main()");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        //List<String> uncompressedimages = new ArrayList<>();

        List<ArrayList<String>> imagesList = new ArrayList<>();
        try {
            ctx.register(ImageCompression.class);
            ctx.refresh();
            Environment env = ctx.getEnvironment();
            Awsproxyconfig awsproxy = new Awsproxyconfig();
            ClientConfiguration client;
            client = awsproxy.getAwsClientWithProxy();
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(env.getProperty("aws.s3AuthKey"), env.getProperty("aws.s3AuthSecretKey"));
            AmazonS3Client s3 = new AmazonS3Client(awsCreds, client);
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(env.getProperty("aws.s3BucketName")).withPrefix(env.getProperty("aws.folder"));
            listObjectsRequest.setMaxKeys(10);
            ObjectListing objectListing;
            Tinify.setKey(env.getProperty("tiny.key"));
            Options options = new Options()
                    .with("service", "s3")
                    .with("aws_access_key_id", env.getProperty("aws.s3AuthKey"))
                    .with("aws_secret_access_key", env.getProperty("aws.s3AuthSecretKey"))
                    .with("region", env.getProperty("aws.region"));
            do {
                objectListing = s3.listObjects(listObjectsRequest);
                ArrayList<String> imagesListChunk = new ArrayList<>();
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
                    String filename = objectSummary.getKey();
                    if (filename.endsWith(".jpg") || filename.endsWith(".png")) {
                        imagesListChunk.add(filename);
                    }
                }
                imagesList.add(imagesListChunk);
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
            imagesList.stream().forEach((ArrayList<String> imagesList1) -> {
                if (!imagesList1.isEmpty()) {
                    final ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(() -> {
                        CompressionUtility.compressImage(imagesList1, options, env);
                });
            }
            
        }  
            );
        } finally {
            ctx.close();
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
