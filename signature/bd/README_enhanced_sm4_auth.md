# 增强版SM4签名认证系统

## 概述

针对原有SM4签名认证机制的安全问题，设计并实现了增强版的认证系统。新系统解决了重放攻击、密钥管理、加密模式等安全隐患。

## 原有系统的安全问题

### 1. 重放攻击风险
- ❌ 缺乏时间戳验证
- ❌ 随机数无防重放机制
- ❌ 同一签名可重复使用

### 2. 加密安全性问题
- ❌ 使用ECB模式，相同明文产生相同密文
- ❌ 缺乏初始化向量(IV)
- ❌ 简单字符串拼接易被伪造

### 3. 密钥管理问题
- ❌ 加密密钥硬编码
- ❌ 缺乏密钥轮换机制

## 增强版系统改进

### 🔒 **安全性增强**

#### 1. 防重放攻击机制
```java
// 时间戳验证（5分钟窗口）
if (Math.abs(currentTime - signatureResult.getTimestamp()) > TIME_WINDOW_MS) {
    return SignatureValidationResult.fail("请求时间戳超出允许范围");
}

// Nonce唯一性验证
if (usedNonceChecker.isNonceUsed(signatureResult.getNonce())) {
    return SignatureValidationResult.fail("请求已处理，请勿重复提交");
}
```

#### 2. 加密模式升级
- ✅ **ECB → CBC模式**：每次加密使用不同的IV
- ✅ **随机IV**：16字节安全随机数
- ✅ **PKCS7填充**：标准填充方式

#### 3. 数据完整性校验
```java
// 请求数据哈希校验
if (requestData != null && !requestData.trim().isEmpty()) {
    String dataHash = DigestUtil.sha256Hex(requestData);
    signData.append("&dataHash=").append(dataHash);
}
```

### 📋 **签名格式改进**

#### 原有格式
```
明文: appId + appSecret + "+" + 8位随机数
```

#### 增强格式
```
明文: appId=xxx&timestamp=xxx&nonce=xxx&appSecret=xxx&dataHash=xxx
```

### 🔄 **认证流程对比**

#### 原有流程
1. 客户端：生成8位随机数
2. 客户端：拼接 `appId + appSecret + "+" + random`
3. 客户端：SM4-ECB加密
4. 服务端：SM4-ECB解密验证

#### 增强流程
1. 客户端：生成时间戳和UUID nonce
2. 客户端：构建结构化签名数据
3. 客户端：生成随机IV
4. 客户端：SM4-CBC加密
5. 服务端：时间戳和nonce验证
6. 服务端：SM4-CBC解密
7. 服务端：数据完整性校验
8. 服务端：标记nonce已使用

## 使用方法

### 1. 客户端签名生成

```java
// 生成签名
EnhancedSM4SignUtils.SignatureResult result = 
    EnhancedSM4SignUtils.generateEnhancedSign(
        "app_001",           // appId
        "secret_001",        // appSecret  
        "16bytes_key_here",  // 16字节密钥
        requestDataJson      // 请求数据JSON
    );

// 设置请求头
headers.put("appId", "app_001");
headers.put("signature", result.getSignature());
headers.put("timestamp", result.getTimestamp().toString());
headers.put("nonce", result.getNonce());
headers.put("iv", result.getIv());
```

### 2. 服务端验证

```java
// 构建签名结果对象
EnhancedSM4SignUtils.SignatureResult signatureResult = 
    new EnhancedSM4SignUtils.SignatureResult(signature, timestamp, nonce, iv);

// 验证签名
EnhancedSM4SignUtils.SignatureValidationResult validationResult = 
    EnhancedSM4SignUtils.verifyEnhancedSign(
        signatureResult,
        expectedAppId,
        expectedAppSecret,
        decryptionKey,
        requestDataJson,
        nonceChecker
    );

if (!validationResult.isSuccess()) {
    // 验证失败处理
    return AjaxResult.error(validationResult.getMessage());
}
```

## 核心组件

### 1. EnhancedSM4SignUtils
- **功能**：增强版SM4签名生成和验证
- **特点**：
  - CBC模式加密
  - 随机IV生成
  - 时间戳和nonce验证
  - 数据完整性校验

### 2. RedisNonceChecker
- **功能**：基于Redis的nonce防重放检查
- **特点**：
  - 分布式nonce存储
  - 自动过期清理
  - 高性能查询

### 3. EnhancedApiAuthController
- **功能**：增强版API认证控制器
- **特点**：
  - 完整的参数验证
  - 统一错误处理
  - 业务逻辑分离

## 配置要求

### Maven依赖
```xml
<dependencies>
    <!-- Bouncy Castle -->
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk15on</artifactId>
        <version>1.70</version>
    </dependency>
    
    <!-- Hutool -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.16</version>
    </dependency>
    
    <!-- Spring Boot Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

## 安全建议

### 1. 密钥管理
- ✅ 使用配置文件或环境变量存储密钥
- ✅ 定期轮换密钥
- ✅ 不同环境使用不同密钥

### 2. 网络安全
- ✅ 强制使用HTTPS传输
- ✅ 设置合理的请求频率限制
- ✅ 记录异常请求日志

### 3. 监控告警
- ✅ 监控签名验证失败率
- ✅ 监控异常IP请求
- ✅ 设置安全事件告警

## 性能优化

### 1. Redis优化
```java
// 使用Redis管道批量操作
// 设置合理的连接池大小
// 使用Redis集群提高可用性
```

### 2. 缓存策略
```java
// 缓存访问密钥信息
// 使用本地缓存减少Redis访问
// 设置合理的缓存过期时间
```

### 3. 异步处理
```java
// 异步记录审计日志
// 异步清理过期nonce
// 使用线程池处理耗时操作
```

## 迁移指南

### 从原有系统迁移

1. **保持兼容性**：
   - 同时支持新旧两套认证机制
   - 逐步迁移客户端
   - 设置迁移截止时间

2. **数据迁移**：
   - 更新访问密钥表结构
   - 配置Redis连接
   - 部署新的认证服务

3. **测试验证**：
   - 单元测试覆盖
   - 集成测试验证
   - 压力测试评估

## 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| AUTH_001 | 请求头参数缺失 | 检查请求头完整性 |
| AUTH_002 | 时间戳超出范围 | 同步客户端时钟 |
| AUTH_003 | 重复请求 | 检查nonce生成逻辑 |
| AUTH_004 | 签名验证失败 | 检查密钥和算法一致性 |
| AUTH_005 | 数据完整性校验失败 | 检查请求数据是否被篡改 |

## 总结

增强版SM4签名认证系统通过以下改进大幅提升了安全性：

- 🔒 **防重放攻击**：时间戳 + nonce机制
- 🔐 **加密强化**：ECB → CBC模式 + 随机IV
- 🛡️ **完整性保护**：SHA256数据哈希校验
- 📊 **可监控性**：详细的验证日志和错误码
- ⚡ **高性能**：Redis缓存 + 异步处理

建议在生产环境中逐步替换原有的认证机制，确保API接口的安全性和可靠性。