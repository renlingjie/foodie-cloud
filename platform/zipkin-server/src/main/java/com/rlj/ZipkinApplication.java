package com.rlj;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import zipkin.server.internal.EnableZipkinServer;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-27
 */
@EnableEurekaClient
@SpringBootApplication
@EnableZipkinServer
public class ZipkinApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ZipkinApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
