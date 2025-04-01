package com.tenxi.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class JwtUtils {
    @Value("${online.education.secret}")
    private static final String JWT_KEY = "";

    private static final Long JWT_EXPIRATION = 60L * 60 * 24 * 7 * 1000;

    // 生成 JWT
    public static String generateJwt(Map<String, String> claims) {
        SecretKey secretKey = generateSecretKey();

        return Jwts.builder()
                .setClaims(claims)
                .setId(createUUID())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    // 解析 JWT
    public static Claims parseJwt(String jwt) {
        SecretKey secretKey = generateSecretKey();

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    // 生成安全密钥（自动处理密钥长度）
    private static SecretKey generateSecretKey() {
        try {
            byte[] keyBytes = JWT_KEY.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            keyBytes = sha.digest(keyBytes);
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate JWT secret key", e);
        }
    }

    // 生成 UUID
    private static String createUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
