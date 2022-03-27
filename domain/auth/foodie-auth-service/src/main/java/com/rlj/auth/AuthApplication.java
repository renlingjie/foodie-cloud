package com.rlj.auth;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-23
 */
@SpringBootApplication
@EnableEurekaClient
public class AuthApplication {
    // auth-service只负责生成jwt-token,由各个业务方(或网关层)在自己的代码里用key校验token的正确性
    // 网关层鉴权优点：符合规范，并且节约了一次HTTP请求()
    public static void main(String[] args) {
        new SpringApplicationBuilder(AuthApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
