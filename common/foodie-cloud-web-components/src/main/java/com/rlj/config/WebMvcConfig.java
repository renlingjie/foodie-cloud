package com.rlj.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 实现静态资源的映射(会将静态资源发布到我们的Web浏览器上)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //在此完成静态资源的注册(注册的静态资源就可以被发布了)
        registry.addResourceHandler("/**")//允许映射所有路径下的资源，之后指定具体的映射位置
                //还要映射swagger2，因为之前没有进行映射的时候，swagger2会自己映射，现在开启映射了，swagger2就会交由我们配置
                //如果我们不进行配置，swagger2就访问不到了
                .addResourceLocations("classpath:/META-INF/resources/")//swagger2是在这个路径下的
                .addResourceLocations("file:/Users/renlingjie/");//映射本地静态资源,images下的资源我们都可以访问到
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    //将拦截器加入到容器中
//    @Bean
//    public UserTokenInterceptor userTokenInterceptor() {
//        return new UserTokenInterceptor();
//    }
//    //注册拦截器并配置拦截的哪些路径
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        //指定拦截方法的路由
//        registry.addInterceptor(userTokenInterceptor())
//                .addPathPatterns("/hello")
//                .addPathPatterns("/userInfo/*");
//        //将最终指定拦截路径的拦截器真正的注册进去
//        WebMvcConfigurer.super.addInterceptors(registry);
//    }
}
