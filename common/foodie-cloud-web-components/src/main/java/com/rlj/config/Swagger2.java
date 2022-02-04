package com.rlj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2 {
    //配置Swagger2核心配置docket
    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.SWAGGER_2)//指定API类型为Swagger2
                .apiInfo(apiInfo())//用于定义API文档汇总信息
                .select().apis(RequestHandlerSelectors.
                        withClassAnnotation(RestController.class))//只要加上@RestController的类都会被抓取到
                .paths(PathSelectors.any())//扫描上面指定包下的所有controller
                .build();
    }
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("天天吃货 电商平台接口API")//文档页标题
                .contact(new Contact("rlj","https://www.rlj.com","30@qq.com"))//联系人信息
                .description("专门为天天吃货提供的API文档信息")//详细信息
                .version("1.0.1")//文档版本号
                .termsOfServiceUrl("https://www.rlj.com")//网站地址
                .build();
    }
}
//默认访问文档的地址为：http://localhost:8088/swagger-ui.html
//优化访问文档的地址为：http://localhost:8088/doc.html