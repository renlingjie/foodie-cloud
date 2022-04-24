package com.rlj;

import com.rlj.filter.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-18
 */
@Configuration
public class RoutesConfiguration {

    @Autowired
    private KeyResolver hostNameResolver;

    @Autowired
    @Qualifier("redisLimiterUser")
    private RateLimiter rateLimiterUser;

    @Autowired
    @Qualifier("redisLimiterItem")
    private RateLimiter rateLimiterItem;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthFilter authFilter) {// 作为参数也是自动注入Bean的一种方式
        return builder.routes()

                /**
                 * 注意事项：
                 * 1、为了做用户登录校验在Auth模块指定了/address/add等，为了负载均衡在User模块指定了/address/**，那么
                 *   "/address/add"能匹配两个模块配置的这个path，那么当这个请求过来了，会进入哪个route中呢？先到先得，
                 *   我们肯定希望先鉴权，所以鉴权模块要放在最前面
                 * 2、这里在这里配置拦截要验证登录的请求路径，并转发到Auth模块，可以改进的：
                 *   I、我们如果用非对称加密，就可以直接在Gateway本地直接验证，不用远程调用Auth
                 *   II、拦截路径写在这里太丑了，我们可以用Config-Server，动态的拉取
                 *   III、也可以使用拦截器去拦截这些路径，而不是放在filter中，这样filter只负责对请求进行鉴权
                 * 3、TODO
                 *   我们f.filter(authFilter)会拿到请求的请求头中的userId来鉴权，这些是在登录的时候在
                 *   后端创建的，所以要返回给前端，并且前端在进行请求的时候把这些信息加入到请求头中才行！！！
                 *   注：如果不好处理，就把这个Auth模块注释掉吧
                 */

                // 1、Auth模块：凡是要验证用户是否登录的请求Url都添加在下面，之后拦截这些请求去自定义的AuthFilter中校验
                // 如果校验失败，返回校验失败的Http状态码。注意！！因为这些请求都是User模块的，所以校验成功就负载均衡这个
                // 请求到User模块。其他模块的请求如果要鉴权，就需要新建route，当然filter不用变，但是uri就改为对应模块的
                .route(r -> r.path("/address/list",
                        "/address/add",
                        "/address/update",
                        "/address/setDefault",
                        "/address/delete",
                        "/userInfo/**",
                        "/center/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://FOODIE-USER-SERVICE")
                )

                // 2、Search模块，前端轮播图、三级列表、ES高亮查询
                .route(r -> r.path("/search/**", "/index/**", "/items/search")
                        .uri("lb://FOODIE-SEARCH-SERVICE")
                )

                // 3、User模块，将所有前端发向用户模块的Url，进行路由规则的配置，也就是web模块的共4个controller的路由
                .route(r -> r.path("/address/**","/passport/**","/userInfo/**","/center/**")
                        .filters(f -> f.requestRateLimiter(c -> {
                            c.setKeyResolver(hostNameResolver);
                            c.setRateLimiter(rateLimiterUser);
                            c.setStatusCode(HttpStatus.BAD_GATEWAY); // 指定超限返回的HttpCode，默认429。这里为502
                        }))
                        .uri("lb://FOODIE-USER-SERVICE")
                )
                // 4、Item模块，同理
                .route(r -> r.path("/items/**")
                        .filters(f -> f.requestRateLimiter(c -> {
                            c.setKeyResolver(hostNameResolver);
                            c.setRateLimiter(rateLimiterItem);
                            c.setStatusCode(HttpStatus.BAD_GATEWAY); // 指定超限返回的HttpCode，默认429。这里为502
                        }))
                        .uri("lb://FOODIE-ITEM-SERVICE")
                )
                // 5、Cart模块，同理
                .route(r -> r.path("/shopcart/**")
                        .uri("lb://FOODIE-CART-SERVICE")
                )
                // 6、Order模块，同理
                .route(r -> r.path("/orders/**","/myorders/**","/mycomments/**")
                        .uri("lb://FOODIE-ORDER-SERVICE")
                )
                .build();
    }
}
