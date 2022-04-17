package com.rlj.search;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

//排除自动装配，避免SpringSession中第一次访问方法、cookie删除访问方法两种情况下时候需要验证登录
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描所有包以及相关组件（ID生成策略组件）包
@ComponentScan(basePackages = {"com.rlj","org.n3r.idworker"})
@EnableEurekaClient
public class SearchApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SearchApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
