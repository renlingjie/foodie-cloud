package com.rlj;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-18
 */
@Configuration
public class RedisLimiterConfiguration {
    /**
     *   Gateway的限流组件要求定义一个KeyResolver用来对每次路由请求生成一个Key，这个Key就是一个限流分组的标识，
     * 每个Key相当于一个令牌桶。假如我们限定了一个服务每秒只能被调用3次，这个限制会对不同的Key单独计数，我们把调
     * 用方机器的HostName作为限流Key，那么来自同一台机器的调用将落到同一个Key下面，也就是说在这个场景下，每台机
     * 器都独立计算单位时间调用量
     *   这个是基于HostName的令牌生成器，也可根据业务来选择合适的Key，从Request中提取业务字段作为Key（如用户ID）
     *
     *   其中的@Primary：在系统中，用户模块可能想使用username作为令牌名，其他模块想使用请求的HostName为令牌名，
     * 那么就需要配置多个KeyResolver，但是@Autowired一个KeyResolver，究竟是哪个，可以加个@Quailfier指定，不过
     * 如果不加那系统不知道是哪个，不就报错了吗？所以@Primary可以指定一个主的，作为默认的
     */
    @Bean
    @Primary  // 因为只有一个KeyResolver，其实可以不指定，但是加上以防以后再添加KeyResolver
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    // Redis限流，有两种构造方法，一个是传入Lua脚本的自定义限流，一个是直接指定令牌桶速率和容量的限流(底层系统已经实现)
    @Bean("redisLimiterUser")
    @Primary  // 有两个RedisRateLimiter，所以必须指定Gateway自动装配的时候预先加载哪个
    public RedisRateLimiter redisRateLimiterUser() {
        return new RedisRateLimiter(1, 2); // 每秒10个令牌，桶容量20
    }

    @Bean("redisLimiterItem")
    public RedisRateLimiter redisRateLimiterItem() {
        return new RedisRateLimiter(20, 50); // 每秒20个令牌，桶容量50
    }
}
