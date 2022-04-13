package com.rlj.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author Renlingjie
 * @name
 * @date 2022-02-01
 */

//扫描mybatis通用mapper所在的包
@MapperScan(basePackages = "com.rlj.cart.mapper")
//排除自动装配，避免SpringSession中第一次访问方法、cookie删除访问方法两种情况下时候需要验证登录
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描所有包以及相关组件（ID生成策略组件）包
@ComponentScan(basePackages = {"com.rlj", "org.n3r.idworker"})
@EnableDiscoveryClient
// 为了actuator中有/hystrix.stream的暴露接口，turbine如果配置要收集这个微服务的信息，不加则没有此接口，会报错
@EnableHystrix
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }

}
