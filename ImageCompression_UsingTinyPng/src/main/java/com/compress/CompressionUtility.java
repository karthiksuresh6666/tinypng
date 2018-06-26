/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compress;

import com.tinify.AccountException;
import com.tinify.ClientException;
import com.tinify.ConnectionException;
import com.tinify.Options;
import com.tinify.ServerException;
import com.tinify.Source;
import com.tinify.Tinify;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 *
 * @author Karthik Suresh
 */
public class CompressionUtility {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CompressionUtility.class);
    
    
    public static Future compressImage(List<String> ImagesList, Options options, Environment env) {
        LOGGER.info("Inside compressImage()");
        List uncompressedimages=new ArrayList<>();
        String awsurl=env.getProperty("aws.url");
        String awsurlcompressedpath=env.getProperty("aws.s3BucketName");
        ImagesList.stream().forEach((filename) -> {
            try {
                Source source = Tinify.fromUrl(awsurl + "/" + filename);
                options.with("path", awsurlcompressedpath + "/Compressed/" + filename);
                source.store(options);
            } catch (AccountException | ClientException ex) {
                System.out.println("AccountException | ClientException" + ex);
                LOGGER.info("{}", ex.getMessage());
                uncompressedimages.add(awsurl + "/" + filename);
            } catch (ServerException e) {
                System.out.println("ServerException" + e);
                LOGGER.info("{}", e.getMessage());
                uncompressedimages.add(awsurl + "/" + filename);
            } catch (ConnectionException e) {
                System.out.println("ConnectionException" + e);
                LOGGER.info("{}", e.getMessage());
                uncompressedimages.add(awsurl + "/" + filename);
            } catch (java.lang.Exception e) {
                System.out.println("java_lang_Exception" + e);
                LOGGER.info("{}", e.getMessage());
                uncompressedimages.add(awsurl + "/" + filename);
            }
        });
        LOGGER.debug("UnCompressedImage :{}" + uncompressedimages);
        return null;
    }
    
    
    
    
    
}
