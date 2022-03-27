package com.rlj.filter;

import com.rlj.auth.service.AuthService;
import com.rlj.auth.service.pojo.Account;
import com.rlj.auth.service.pojo.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.rlj.auth.service.pojo.AuthCode.SUCCESS;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-24
 */
@Slf4j
@Component("authFilter")
public class AuthFilter implements GatewayFilter, Ordered {

    private static final String AUTH = "Authorization";      // 请求头中的Authorization属性
    private static final String USERID = "rlj-user-id";

    @Autowired
    private AuthService authService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Auth start");

        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders header = request.getHeaders();
        String token = header.getFirst(AUTH);
        String userId = header.getFirst(USERID);

        ServerHttpResponse response = exchange.getResponse();
        if (StringUtils.isBlank(token)){
            log.error("token not found");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        Account account = Account.builder().token(token).userId(userId).build();

        // 在这里调用Auth服务的解密认证方法
        AuthResponse verify = authService.verifyToken(account);
        if (verify.getCode() != SUCCESS.getCode()){
            log.error("Invalid token");
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        // 走到这里校验成功
        ServerHttpRequest.Builder mutate = request.mutate();
        mutate.header(USERID, userId);
        mutate.header(AUTH, token);
        ServerHttpRequest buildRequest = mutate.build();

        response.getHeaders().add(USERID, userId);
        response.getHeaders().add(AUTH, token);
        return chain.filter(exchange.mutate().request(buildRequest).response(response).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
