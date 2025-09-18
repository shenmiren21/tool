package com.haedu.common.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 增强版SM4签名工具类
 * 解决原版本的安全问题，增加防重放攻击机制
 */
public class EnhancedSM4SignUtils {

    static {
        // 注册Bouncy Castle Provider
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // 时间窗口：5分钟（防重放攻击）
    private static final long TIME_WINDOW_MS = 5 * 60 * 1000;

    /**
     * 生成增强版SM4签名
     * 
     * @param appId     应用ID
     * @param appSecret 应用密钥
     * @param key       16字节的加密密钥
     * @param requestData 请求数据（可选，用于数据完整性校验）
     * @return 签名结果对象
     */
    public static SignatureResult generateEnhancedSign(String appId, String appSecret, String key, String requestData) {
        try {
            // 生成时间戳和随机数
            long timestamp = System.currentTimeMillis();
            String nonce = IdUtil.fastSimpleUUID();
            
            // 构建签名数据
            StringBuilder signData = new StringBuilder();
            signData.append("appId=").append(appId)
                   .append("&timestamp=").append(timestamp)
                   .append("&nonce=").append(nonce)
                   .append("&appSecret=").append(appSecret);
            
            // 如果有请求数据，加入签名计算
            if (requestData != null && !requestData.trim().isEmpty()) {
                String dataHash = DigestUtil.sha256Hex(requestData);
                signData.append("&dataHash=").append(dataHash);
            }
            
            // 生成16字节随机IV
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            
            // SM4加密（CBC模式）
            String encryptedSign = encryptWithSM4CBC(signData.toString(), key, iv);
            
            return new SignatureResult(encryptedSign, timestamp, nonce, Base64.getEncoder().encodeToString(iv));
            
        } catch (Exception e) {
            throw new RuntimeException("增强签名生成失败", e);
        }
    }

    /**
     * 验证增强版SM4签名
     * 
     * @param signatureResult 签名结果对象
     * @param expectedAppId   期望的应用ID
     * @param expectedAppSecret 期望的应用密钥
     * @param key            解密密钥
     * @param requestData    请求数据（用于完整性校验）
     * @param usedNonceChecker 已使用nonce检查器
     * @return 验证结果
     */
    public static SignatureValidationResult verifyEnhancedSign(
            SignatureResult signatureResult, 
            String expectedAppId, 
            String expectedAppSecret, 
            String key,
            String requestData,
            UsedNonceChecker usedNonceChecker) {
        
        try {
            // 1. 时间戳验证（防重放攻击）
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - signatureResult.getTimestamp()) > TIME_WINDOW_MS) {
                return SignatureValidationResult.fail("请求时间戳超出允许范围");
            }
            
            // 2. Nonce验证（防重复请求）
            if (usedNonceChecker.isNonceUsed(signatureResult.getNonce())) {
                return SignatureValidationResult.fail("请求已处理，请勿重复提交");
            }
            
            // 3. 解密签名
            byte[] iv = Base64.getDecoder().decode(signatureResult.getIv());
            String decryptedData = decryptWithSM4CBC(signatureResult.getSignature(), key, iv);
            
            // 4. 解析签名数据
            SignatureData parsedData = parseSignatureData(decryptedData);
            
            // 5. 验证应用信息
            if (!expectedAppId.equals(parsedData.getAppId())) {
                return SignatureValidationResult.fail("AppId不匹配");
            }
            if (!expectedAppSecret.equals(parsedData.getAppSecret())) {
                return SignatureValidationResult.fail("AppSecret不匹配");
            }
            
            // 6. 验证时间戳一致性
            if (!signatureResult.getTimestamp().equals(parsedData.getTimestamp())) {
                return SignatureValidationResult.fail("时间戳不一致");
            }
            
            // 7. 验证nonce一致性
            if (!signatureResult.getNonce().equals(parsedData.getNonce())) {
                return SignatureValidationResult.fail("随机数不一致");
            }
            
            // 8. 验证请求数据完整性
            if (requestData != null && !requestData.trim().isEmpty()) {
                String expectedDataHash = DigestUtil.sha256Hex(requestData);
                if (parsedData.getDataHash() == null || !expectedDataHash.equals(parsedData.getDataHash())) {
                    return SignatureValidationResult.fail("请求数据完整性校验失败");
                }
            }
            
            // 9. 标记nonce已使用
            usedNonceChecker.markNonceAsUsed(signatureResult.getNonce());
            
            return SignatureValidationResult.success("签名验证成功");
            
        } catch (Exception e) {
            return SignatureValidationResult.fail("签名验证异常: " + e.getMessage());
        }
    }

    /**
     * SM4 CBC模式加密
     */
    private static String encryptWithSM4CBC(String plaintext, String key, byte[] iv) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        validateKey(keyBytes);
        
        Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * SM4 CBC模式解密
     */
    private static String decryptWithSM4CBC(String ciphertext, String key, byte[] iv) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        validateKey(keyBytes);
        
        Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * 解析签名数据
     */
    private static SignatureData parseSignatureData(String signatureData) {
        SignatureData data = new SignatureData();
        String[] pairs = signatureData.split("&");
        
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                switch (kv[0]) {
                    case "appId":
                        data.setAppId(kv[1]);
                        break;
                    case "timestamp":
                        data.setTimestamp(Long.parseLong(kv[1]));
                        break;
                    case "nonce":
                        data.setNonce(kv[1]);
                        break;
                    case "appSecret":
                        data.setAppSecret(kv[1]);
                        break;
                    case "dataHash":
                        data.setDataHash(kv[1]);
                        break;
                }
            }
        }
        
        return data;
    }

    /**
     * 验证密钥长度
     */
    private static void validateKey(byte[] keyBytes) {
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("密钥必须为16字节（128位）");
        }
    }

    /**
     * 签名结果类
     */
    public static class SignatureResult {
        private String signature;
        private Long timestamp;
        private String nonce;
        private String iv;

        public SignatureResult(String signature, Long timestamp, String nonce, String iv) {
            this.signature = signature;
            this.timestamp = timestamp;
            this.nonce = nonce;
            this.iv = iv;
        }

        // Getters
        public String getSignature() { return signature; }
        public Long getTimestamp() { return timestamp; }
        public String getNonce() { return nonce; }
        public String getIv() { return iv; }
    }

    /**
     * 签名数据类
     */
    private static class SignatureData {
        private String appId;
        private Long timestamp;
        private String nonce;
        private String appSecret;
        private String dataHash;

        // Getters and Setters
        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public String getNonce() { return nonce; }
        public void setNonce(String nonce) { this.nonce = nonce; }
        public String getAppSecret() { return appSecret; }
        public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
        public String getDataHash() { return dataHash; }
        public void setDataHash(String dataHash) { this.dataHash = dataHash; }
    }

    /**
     * 签名验证结果类
     */
    public static class SignatureValidationResult {
        private boolean success;
        private String message;

        private SignatureValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SignatureValidationResult success(String message) {
            return new SignatureValidationResult(true, message);
        }

        public static SignatureValidationResult fail(String message) {
            return new SignatureValidationResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    /**
     * 已使用Nonce检查器接口
     */
    public interface UsedNonceChecker {
        boolean isNonceUsed(String nonce);
        void markNonceAsUsed(String nonce);
    }
}