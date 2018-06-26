/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import com.amazonaws.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;

/**
 *
 * @author Karthik Suresh
 */
@Configuration
@PropertySources({
    @PropertySource("classpath:application.properties"),
    @PropertySource(value = "file:application.properties", ignoreResourceNotFound = true)
})
public class Awsproxyconfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(Awsproxyconfig.class);

    public final ClientConfiguration getAwsClientWithProxy() {
        LOGGER.info("Inside getAwsClientWithProxy()");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ClientConfiguration client = new ClientConfiguration();
        try {
            ctx.register(Awsproxyconfig.class);
            ctx.refresh();
            Environment env = ctx.getEnvironment();
            client.setProxyUsername( env.getProperty("aws.proxyUserName"));
            client.setProxyPassword( env.getProperty("aws.proxyPassword"));
        } finally {
            ctx.close();
        }
        return client;
    }
}