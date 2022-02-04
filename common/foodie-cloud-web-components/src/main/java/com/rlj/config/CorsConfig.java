package com.rlj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    public CorsConfig(){
    }
    @Bean//web.filter.CorsFilter
    public CorsFilter corsFilter(){
        //1、添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8080");//添加允许的跨域信息的内容
        config.addAllowedOrigin("http://foodieshop.ltd:8080");//添加允许的跨域信息的内容
        config.addAllowedOrigin("http://foodieshop.ltd");//添加允许的跨域信息的内容
        config.addAllowedOrigin("http://8.141.50.179:8080");//添加允许的跨域信息的内容
        config.addAllowedOrigin("http://8.141.50.179");//为以后作准备，因为Nginx是不会有端口的
        //是否允许我们的请求携带一些内容，因为我们要用cookie、session，所以这里我们设置为true
        config.setAllowCredentials(true);
        //设置允许请求的方式，比如说get、post，这里允许所有的请求方式，所以这里是*
        config.addAllowedMethod("*");
        //设置允许的Header(也就是说请求头中可以放哪些信息)
        config.addAllowedHeader("*");
        //2、为url添加映射路径(也就是说上面的配置能被哪些路径所使用)
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();//这里是没有reactive的那个
        corsSource.registerCorsConfiguration("/**",config);//允许所有的请求路径使用上面的配置信息
        //3、返回重新定义好的corsSource
        return new CorsFilter(corsSource);
    }
}
