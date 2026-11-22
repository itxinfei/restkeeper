package com.restkeeper.utils;

import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类（jjwt 0.12.x API）。
 * 过渡期桥接：Sa-Token 落地后随自研鉴权代码一并删除。
 */
@Slf4j
public class JWTUtil {

    /** HS256 要求密钥至少 256 位（32 字节），旧配置长度不足时补零，避免 WeakKeyException */
    private static final int MIN_KEY_LENGTH = 32;

    /** token 有效期：3 天 */
    private static final int EXPIRE_DAYS = 3;

    /**
     * 签发JWT
     *
     * @param tokenMap 业务声明
     * @param secret   Base64 编码密钥
     */
    public static String createJWTByObj(Map<String, Object> tokenMap, String secret) {
        SecretKey secretKey = generalKey(secret);

        ZoneId zoneId = ZoneId.systemDefault();
        Date now = new Date();
        Date expiration = Date.from(LocalDateTime.now().plusDays(EXPIRE_DAYS).atZone(zoneId).toInstant());

        return Jwts.builder()
                .id("restkeeper")
                .issuedAt(now)
                .claims(tokenMap)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 验证jwt
     * 5001:token过期;5002:无效token
     */
    public static VerifyResult verifyJwt(String token, String secret) {
        SecretKey key = generalKey(secret);
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            VerifyResult result = new VerifyResult(true, ResultCode.success);
            result.setTokenMap(claims);
            return result;
        } catch (ExpiredJwtException e) {
            log.warn("token 已过期");
            return new VerifyResult(false, 5001);
        } catch (Exception e) {
            log.warn("无效 token: {}", e.getMessage());
            return new VerifyResult(false, 5002);
        }
    }

    /**
     * 不验签解析 payload（仅用于读取业务声明，鉴权以 verifyJwt 为准）
     */
    public static Map<String, Object> decode(String token) {
        String bodyData = token.split("\\.")[1];
        String bodyStr = new String(Decoders.BASE64.decode(bodyData), StandardCharsets.UTF_8);
        return JSON.parseObject(bodyStr, Map.class);
    }

    /**
     * 生成 HMAC-SHA 密钥
     */
    private static SecretKey generalKey(String jwtSecret) {
        byte[] encodedKey = Decoders.BASE64.decode(jwtSecret);
        if (encodedKey.length < MIN_KEY_LENGTH) {
            byte[] padded = new byte[MIN_KEY_LENGTH];
            System.arraycopy(encodedKey, 0, padded, 0, encodedKey.length);
            encodedKey = padded;
        }
        return Keys.hmacShaKeyFor(encodedKey);
    }

    @Data
    public static class VerifyResult {
        private boolean isValidate;
        /**
         * 5001:token过期;5002:无效token
         */
        private int code;

        private Map<String, Object> tokenMap;

        public VerifyResult(boolean isValidate, int code) {
            this.isValidate = isValidate;
            this.code = code;
        }
    }
}
