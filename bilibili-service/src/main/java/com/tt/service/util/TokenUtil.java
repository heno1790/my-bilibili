package com.tt.service.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tt.domain.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

/**
 * ClassName: TokenUtil
 * Package: com.tt.service.util
 * Description:
 *
 * @Create 2025/3/18 15:27
 */
public class TokenUtil {
    private static final String ISSUER = "tt";  //令牌的签发者，公司名称或者本人名字之类的

    public static String generateToken(Long userId) throws Exception {
        //指定jwt令牌的加密算法，调用RSA
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 7200);  // 设置令牌过期时间为7200s
        //生成令牌
        return JWT.create().withKeyId(String.valueOf(userId))  //用户id
                .withIssuer(ISSUER)  //签发者
                .withExpiresAt(calendar.getTime())  //过期时间
                .sign(algorithm);  //加密
    }

    public static Long verifyToken(String token) { //验证令牌，即使过期了也能得到用户id
        try {
            //同样指定jwt令牌的加密算法
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
            //生成jwt令牌验证器
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            //解密后的令牌中可以提取出用户id了(当然前提是能加密成功的话)
            String userId = jwt.getKeyId();
            return Long.valueOf(userId);
        } catch (TokenExpiredException e) {
            // 手动解码令牌以获取userId（即使过期）
            DecodedJWT expiredJwt = JWT.decode(token); // 直接解码，不验证
            String userId = expiredJwt.getKeyId();
            throw new ConditionException("777", "Token过期，用户ID: " + userId);
        } catch (Exception e) {
            //暂且认为除了过期令牌以外,所有解析异常的令牌都是非法用户令牌
            throw new ConditionException("非法的token");
        }
    }

    public static String generateRefreshToken(long userId) throws Exception {
        //指定jwt令牌的加密算法
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(),RSAUtil.getPrivateKey());
        Calendar calendar=Calendar.getInstance();
        //设置令牌过期时间为100天
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH,100);
        //生成令牌
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
    }

    public static Long verifyrefreshToken(String refreshToken) {
        try {
            //指定jwt令牌的算法
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(),RSAUtil.getPrivateKey());
            //生成jwt令牌验证器
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(refreshToken);
            //解密后的令牌中可以提取出用户id了(当然前提是能解析成功的话)
            String userId = jwt.getKeyId();
            return Long.valueOf(userId);
        } catch (TokenExpiredException e) {
            throw new ConditionException("666","当前处于未登录状态,原因是refreshtoken令牌过期,请重新登录");
        } catch(Exception e){
            throw new ConditionException("非法的refreshtoken");
        }
    }
}
