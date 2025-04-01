package com.tenxi.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 用于生成hmac签名的工具类
 * 在网关和微服务、微服务使用Open Feign之间传递用户信息时的加密
 */
public class HmacSigner {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SECRET_KEY = "my-temp-secret-key-123"; // 密钥（实际应配置在安全位置）

    /**
     * 生成 HMAC 签名
     * @param data 待签名的数据
     * @return Base64 编码的签名
     */
    public static String sign(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC 签名生成失败", e);
        }
    }

    /**
     * 验证签名是否合法
     * @param data 原始数据
     * @param signature 待验证的签名
     * @return 是否验证通过
     */
    public static boolean verify(String data, String signature) {
        String expectedSignature = HmacSigner.sign(data);
        return expectedSignature.equals(signature);
    }
}
