package com.rlj.order;

import com.rlj.item.service.ItemService;
import com.rlj.order.fallback.itemservice.ItemCommentsFeignService;
import com.rlj.user.service.AddressService;
import com.rlj.user.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.concurrent.TimeUnit;

/**
 * @author Renlingjie
 * @name
 * @date 2022-02-01
 */
//扫描mybatis通用mapper所在的包
@MapperScan(basePackages = "com.rlj.order.mapper")
//排除自动装配，避免SpringSession中第一次访问方法、cookie删除访问方法两种情况下时候需要验证登录
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描所有包以及相关组件（ID生成策略组件）包
@ComponentScan(basePackages = {"com.rlj","org.n3r.idworker"})
// 为了actuator中有/hystrix.stream的暴露接口，turbine如果配置要收集这个微服务的信息，不加则没有此接口，会报错
@EnableHystrix
@EnableDiscoveryClient
//@EnableFeignClients(basePackages = {
//        "com.rlj.user.service","com.rlj.item.service","com.rlj.order.fallback.itemservice"})

@EnableFeignClients(clients = {
        ItemService.class, ItemCommentsFeignService.class, UserService.class, AddressService.class})
/**
 * 因为使用Feign中的Hystrix降级，所以我们将item(生产者)提供的接口Jar中com.rlj.item.service的ItemCommentsService
 * 摒弃不用，需要重新写一个接口才能编辑@FeignClient，才能写里面的fallback，但这样原来的ItemCommentsService和新建的
 * ItemCommentsFeignService因为有相同的http路径，所以会报错Ambiguous mapping。因此要把原来的ItemCommentsService
 * 这个远程Feign接口，从@EnableFeignClients的扫描范围中剔除，所以不使用包扫描了。而是点对点，指定要扫描哪些Feign接口
 */
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }
}
