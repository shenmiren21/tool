package com.haedu.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的Nonce检查器实现
 * 用于防重放攻击
 */
@Component
public class RedisNonceChecker implements EnhancedSM4SignUtils.UsedNonceChecker {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // Nonce缓存过期时间：10分钟
    private static final long NONCE_EXPIRE_MINUTES = 10;
    
    // Redis key前缀
    private static final String NONCE_KEY_PREFIX = "api_nonce:";
    
    @Override
    public boolean isNonceUsed(String nonce) {
        String key = NONCE_KEY_PREFIX + nonce;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public void markNonceAsUsed(String nonce) {
        String key = NONCE_KEY_PREFIX + nonce;
        redisTemplate.opsForValue().set(key, "used", NONCE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
}