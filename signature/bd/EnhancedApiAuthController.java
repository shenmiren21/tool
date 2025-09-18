package com.haedu.controller;

import com.haedu.common.utils.EnhancedSM4SignUtils;
import com.haedu.common.utils.RedisNonceChecker;
import com.haedu.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 增强版API认证控制器
 * 使用改进的SM4签名验证机制
 */
@RestController
@RequestMapping("/api/v2")
public class EnhancedApiAuthController {
    
    @Autowired
    private RedisNonceChecker nonceChecker;
    
    @Autowired
    private SysAccessKeyService accessKeyService; // 假设存在此服务
    
    /**
     * 需要签名验证的API接口
     */
    @PostMapping("/secure-data")
    public AjaxResult secureDataEndpoint(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        
        try {
            // 1. 提取请求头参数
            String appId = request.getHeader("appId");
            String signature = request.getHeader("signature");
            String timestamp = request.getHeader("timestamp");
            String nonce = request.getHeader("nonce");
            String iv = request.getHeader("iv");
            
            // 2. 参数验证
            if (StringUtils.isEmpty(appId)) {
                return AjaxResult.error("请求头中缺少appId");
            }
            if (StringUtils.isEmpty(signature)) {
                return AjaxResult.error("请求头中缺少signature");
            }
            if (StringUtils.isEmpty(timestamp)) {
                return AjaxResult.error("请求头中缺少timestamp");
            }
            if (StringUtils.isEmpty(nonce)) {
                return AjaxResult.error("请求头中缺少nonce");
            }
            if (StringUtils.isEmpty(iv)) {
                return AjaxResult.error("请求头中缺少iv");
            }
            
            // 3. 查询访问密钥信息
            SysAccessKey accessKey = accessKeyService.selectAccessKeyByAppId(appId);
            if (accessKey == null) {
                return AjaxResult.error("无效的appId");
            }
            
            // 4. 验证请求路径（可选）
            if (!request.getRequestURI().equalsIgnoreCase(accessKey.getApiPath().trim())) {
                return AjaxResult.error("请求路径不匹配");
            }
            
            // 5. 构建签名结果对象
            EnhancedSM4SignUtils.SignatureResult signatureResult = 
                new EnhancedSM4SignUtils.SignatureResult(signature, Long.parseLong(timestamp), nonce, iv);
            
            // 6. 验证签名
            String requestDataJson = convertToJson(requestBody); // 将请求体转为JSON字符串
            EnhancedSM4SignUtils.SignatureValidationResult validationResult = 
                EnhancedSM4SignUtils.verifyEnhancedSign(
                    signatureResult,
                    appId,
                    accessKey.getSecretKey(),
                    accessKey.getAccessKey(), // 用作SM4加密密钥
                    requestDataJson,
                    nonceChecker
                );
            
            // 7. 处理验证结果
            if (!validationResult.isSuccess()) {
                return AjaxResult.error("签名验证失败: " + validationResult.getMessage());
            }
            
            // 8. 签名验证成功，处理业务逻辑
            Object result = processBusinessLogic(requestBody, accessKey);
            
            return AjaxResult.success("请求处理成功", result);
            
        } catch (NumberFormatException e) {
            return AjaxResult.error("时间戳格式错误");
        } catch (Exception e) {
            return AjaxResult.error("请求处理异常: " + e.getMessage());
        }
    }
    
    /**
     * 生成客户端签名示例接口（仅用于测试）
     */
    @PostMapping("/generate-signature")
    public AjaxResult generateSignatureExample(@RequestBody Map<String, Object> requestBody) {
        try {
            String appId = "test_app_001";
            String appSecret = "test_secret_001";
            String key = "test_key_16bytes"; // 16字节密钥
            
            String requestDataJson = convertToJson(requestBody);
            EnhancedSM4SignUtils.SignatureResult signatureResult = 
                EnhancedSM4SignUtils.generateEnhancedSign(appId, appSecret, key, requestDataJson);
            
            return AjaxResult.success("签名生成成功", Map.of(
                "appId", appId,
                "signature", signatureResult.getSignature(),
                "timestamp", signatureResult.getTimestamp(),
                "nonce", signatureResult.getNonce(),
                "iv", signatureResult.getIv(),
                "usage", "将这些值放入请求头中：appId, signature, timestamp, nonce, iv"
            ));
            
        } catch (Exception e) {
            return AjaxResult.error("签名生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    private String convertToJson(Object obj) {
        // 这里可以使用Jackson、Gson或其他JSON库
        // 示例使用简单的toString，实际应该使用JSON序列化
        if (obj == null) {
            return "";
        }
        // 实际实现应该使用：
        // return objectMapper.writeValueAsString(obj);
        return obj.toString();
    }
    
    /**
     * 处理业务逻辑
     */
    private Object processBusinessLogic(Map<String, Object> requestBody, SysAccessKey accessKey) {
        // 实现具体的业务逻辑
        return Map.of(
            "message", "业务处理成功",
            "appId", accessKey.getAppId(),
            "processedAt", System.currentTimeMillis(),
            "data", requestBody
        );
    }
    
    /**
     * 健康检查接口（无需签名）
     */
    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success("服务正常运行", Map.of(
            "timestamp", System.currentTimeMillis(),
            "version", "v2.0-enhanced"
        ));
    }
}