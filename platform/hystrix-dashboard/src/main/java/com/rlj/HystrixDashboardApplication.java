package com.rlj;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * @author Renlingjie
 * @name
 * @date 2022-02-11
 */
// Dashboard注解
@EnableHystrixDashboard
// 是@SpringBootApplication/@EnableDiscoveryClient/@EnableCircuitBreaker的合集
@SpringCloudApplication
public class HystrixDashboardApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HystrixDashboardApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
