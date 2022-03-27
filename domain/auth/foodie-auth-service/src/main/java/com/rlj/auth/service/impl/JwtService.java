package com.rlj.auth.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.rlj.auth.service.pojo.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Renlingjie
 * @name
 * @date 2022-03-23
 */
@Slf4j
@Service
public class JwtService {
    // 这个key非常重要，开发人一般员是接触不到的，会在外部加密后再给开发人员，这里就直接明文定义一个
    private static final String KEY = "rljkey";
    // 为了代码的健壮性，即使外界破解了上面的key，没有ISSURE也不行(其实也需要加密，这里也是明文定义)
    private static final String ISSUER = "rlj";
    // 过期时间1h
    private static final long TOKEN_EXPIRE_TIME = 24 * 3600 * 1000;

    private static final String USER_ID=  "userId";

    /**
     * 加密算法生成Token(但凡调用这个方法的，说明Login都验证成功了)
     * @param account
     * @return
     */
    public String generateToken(Account account){
        Date now = new Date();
        // 根据这个key生成一个加密算法
        Algorithm algorithm = Algorithm.HMAC256(KEY);
        String token = JWT.create().withIssuer(ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + TOKEN_EXPIRE_TIME))
                .withClaim(USER_ID,account.getUserId()).sign(algorithm);
        log.info("Jwt generated userId = {}",account.getUserId());
        return token;
    }

    /**
     * 校验Token：验证传入的Token是否是当前用户的
     * @param token
     * @param userId
     * @return
     */
    public boolean verify(String token,String userId){
        log.info("verifying jwt, userId = {}",userId);
        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            // 前面定义的东西都可以去验证。这里依次验证：ISSUER是否是上面的ISSUER、Token里面的username是否是传入的userId
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).withClaim(USER_ID,userId).build();
            // 开始验证
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.error("auth failed",e);
            return false;
        }
    }
}
