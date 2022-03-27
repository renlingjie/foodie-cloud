package com.rlj.user;

import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Renlingjie
 * @name
 * @date 2022-02-01
 */
//扫描mybatis通用mapper所在的包
@MapperScan(basePackages = "com.rlj.user.mapper")
//排除自动装配，避免SpringSession中第一次访问方法、cookie删除访问方法两种情况下时候需要验证登录
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描所有包以及相关组件（ID生成策略组件）包
@ComponentScan(basePackages = {"com.rlj","org.n3r.idworker"})
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableFeignClients(basePackages = {"com.rlj.auth"})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
}
