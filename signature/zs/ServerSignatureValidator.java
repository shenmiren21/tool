package signature.zs;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 服务端签名验证器
 * 用于验证客户端发送的签名请求
 */
@Component
public class ServerSignatureValidator {
    
    private static final Logger log = LoggerFactory.getLogger(ServerSignatureValidator.class);
    
    // 签名相关常量（与客户端保持一致）
    private static final String GYS_ACCESS_ID = "gys_access_id";
    private static final String GYS_TIMESTAMP = "gys_timestamp";
    private static final String GYS_NONCE = "gys_nonce";
    private static final String GYS_SIGNATURE = "gys_signature";
    
    // 时间窗口：5分钟（防重放攻击）
    private static final long TIME_WINDOW_MS = 5 * 60 * 1000;
    
    // Nonce缓存过期时间：10分钟
    private static final long NONCE_EXPIRE_MINUTES = 10;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private AppSecretService appSecretService; // 假设有这个服务来获取AppSecret
    
    /**
     * 验证请求签名
     * @param headers 请求头参数
     * @param requestParams 请求体参数
     * @return 验证结果
     */
    public SignatureValidationResult validateSignature(Map<String, String> headers, Map<String, Object> requestParams) {
        try {
            // 1. 提取签名相关参数
            String accessId = headers.get(GYS_ACCESS_ID);
            String timestamp = headers.get(GYS_TIMESTAMP);
            String nonce = headers.get(GYS_NONCE);
            String clientSignature = headers.get(GYS_SIGNATURE);
            
            // 2. 参数验证
            if (StrUtil.isBlank(accessId) || StrUtil.isBlank(timestamp) || 
                StrUtil.isBlank(nonce) || StrUtil.isBlank(clientSignature)) {
                return SignatureValidationResult.fail("签名参数不完整");
            }
            
            // 3. 时间戳验证（防重放攻击）
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - requestTime) > TIME_WINDOW_MS) {
                return SignatureValidationResult.fail("请求时间戳超出允许范围");
            }
            
            // 4. Nonce验证（防重复请求）
            String nonceKey = "signature_nonce:" + nonce;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(nonceKey))) {
                return SignatureValidationResult.fail("请求已处理，请勿重复提交");
            }
            
            // 5. 获取AppSecret
            String appSecret = appSecretService.getAppSecret(accessId);
            if (StrUtil.isBlank(appSecret)) {
                return SignatureValidationResult.fail("无效的AccessId");
            }
            
            // 6. 重新计算签名
            String serverSignature = calculateSignature(headers, requestParams, appSecret);
            
            // 7. 签名对比
            if (!clientSignature.equals(serverSignature)) {
                log.warn("签名验证失败 - AccessId: {}, 客户端签名: {}, 服务端签名: {}", 
                        accessId, clientSignature, serverSignature);
                return SignatureValidationResult.fail("签名验证失败");
            }
            
            // 8. 记录已使用的Nonce
            redisTemplate.opsForValue().set(nonceKey, "used", NONCE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            log.info("签名验证成功 - AccessId: {}", accessId);
            return SignatureValidationResult.success(accessId);
            
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return SignatureValidationResult.fail("签名验证异常: " + e.getMessage());
        }
    }
    
    /**
     * 计算服务端签名
     * @param headers 请求头参数
     * @param requestParams 请求参数
     * @param appSecret 应用密钥
     * @return 计算得到的签名
     */
    private String calculateSignature(Map<String, String> headers, Map<String, Object> requestParams, String appSecret) {
        // 1. 合并所有参数
        Map<String, Object> allParams = new HashMap<>();
        if (requestParams != null) {
            allParams.putAll(requestParams);
        }
        
        // 2. 添加签名参数（除了signature本身）
        allParams.put(GYS_ACCESS_ID, headers.get(GYS_ACCESS_ID));
        allParams.put(GYS_TIMESTAMP, headers.get(GYS_TIMESTAMP));
        allParams.put(GYS_NONCE, headers.get(GYS_NONCE));
        
        // 3. 参数排序拼接（与客户端保持一致）
        String requestParamsStr = MapUtil.sortJoin(allParams, "&", "=", true);
        
        // 4. 生成签名（这里假设使用HMAC-SHA256，具体算法需要与客户端的generateSignature方法一致）
        return generateSignature(appSecret, requestParamsStr);
    }
    
    /**
     * 生成签名（需要与客户端的generateSignature方法保持一致）
     * 这里假设使用HMAC-SHA256算法
     * @param secret 密钥
     * @param data 待签名数据
     * @return 签名结果
     */
    private String generateSignature(String secret, String data) {
        // 注意：这里的实现需要与客户端的generateSignature方法完全一致
        // 常见的签名算法有：
        // 1. HMAC-SHA256
        // 2. MD5
        // 3. SHA1
        // 4. 自定义算法
        
        // 示例：使用HMAC-SHA256
        return DigestUtil.hmacSha256Hex(secret, data);
        
        // 如果客户端使用的是MD5，则使用：
        // return DigestUtil.md5Hex(secret + data);
        
        // 如果客户端使用的是SHA1，则使用：
        // return DigestUtil.sha1Hex(secret + data);
    }
    
    /**
     * 签名验证结果类
     */
    public static class SignatureValidationResult {
        private boolean success;
        private String message;
        private String accessId;
        
        private SignatureValidationResult(boolean success, String message, String accessId) {
            this.success = success;
            this.message = message;
            this.accessId = accessId;
        }
        
        public static SignatureValidationResult success(String accessId) {
            return new SignatureValidationResult(true, "验证成功", accessId);
        }
        
        public static SignatureValidationResult fail(String message) {
            return new SignatureValidationResult(false, message, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getAccessId() { return accessId; }
    }
}