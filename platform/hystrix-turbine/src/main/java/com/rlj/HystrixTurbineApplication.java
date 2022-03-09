package com.rlj;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.turbine.EnableTurbine;

/**
 * @author Renlingjie
 * @name
 * @date 2022-02-11
 */
@EnableDiscoveryClient
@EnableHystrix
@EnableTurbine
//@SpringBootApplication包含@EnableAutoConfiguration，不过这里没用到其他，只需开启自动装配即可
@EnableAutoConfiguration
public class HystrixTurbineApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HystrixTurbineApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
