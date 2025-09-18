# 服务端签名验证系统

## 概述

基于客户端代码分析，推理并实现了对应的服务端签名验证系统。该系统采用HMAC签名机制，确保API请求的安全性和完整性。

## 核心组件

### 1. ServerSignatureValidator（签名验证器）
- **功能**：验证客户端发送的签名请求
- **安全机制**：
  - 时间戳验证（防重放攻击）
  - Nonce验证（防重复请求）
  - 签名对比验证（确保数据完整性）

### 2. AppSecretService（密钥管理服务）
- **功能**：根据AccessId获取对应的AppSecret
- **实现方式**：支持数据库查询和内存缓存

### 3. SignatureController（控制器示例）
- **功能**：展示如何在Spring Boot中集成签名验证
- **特点**：统一的错误处理和响应格式

## 签名验证流程

### 客户端签名生成
1. 构建签名参数：`access_id`、`timestamp`、`nonce`
2. 合并业务参数和签名参数
3. 按key排序并拼接成字符串
4. 使用AppSecret生成签名

### 服务端验证流程
1. **参数提取**：从请求头提取签名相关参数
2. **安全验证**：
   - 检查时间戳是否在允许窗口内（5分钟）
   - 验证nonce是否已使用过
   - 验证AccessId是否有效
3. **签名重算**：使用相同算法重新计算签名
4. **对比验证**：比较客户端签名与服务端计算的签名
5. **记录状态**：将已使用的nonce存入Redis

## 安全特性

### 防重放攻击
- **时间窗口限制**：请求时间戳必须在当前时间±5分钟内
- **配置项**：`TIME_WINDOW_MS = 5 * 60 * 1000`

### 防重复请求
- **Nonce机制**：每个请求使用唯一的随机数
- **缓存存储**：已使用的nonce存储在Redis中，过期时间10分钟

### 数据完整性
- **签名验证**：确保请求参数未被篡改
- **密钥管理**：AppSecret安全存储，支持动态更新

## 配置要求

### 依赖项
```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Data Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Hutool工具库 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.16</version>
    </dependency>
    
    <!-- JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
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
```

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 使用示例

### 1. 客户端请求示例
```java
// 客户端代码（已提供）
Map<String, String> headers = generateHeaderMap(requestParams);
// headers包含：gys_access_id, gys_timestamp, gys_nonce, gys_signature
```

### 2. 服务端验证示例
```java
@PostMapping("/api/secure-endpoint")
public ResponseEntity<?> secureEndpoint(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
    Map<String, String> headers = extractHeaders(request);
    SignatureValidationResult result = signatureValidator.validateSignature(headers, requestBody);
    
    if (!result.isSuccess()) {
        return ResponseEntity.badRequest().body(Map.of("message", result.getMessage()));
    }
    
    // 处理业务逻辑
    return ResponseEntity.ok(processBusinessLogic(requestBody));
}
```

## 签名算法

### 当前实现
- **算法**：HMAC-SHA256
- **实现**：`DigestUtil.hmacSha256Hex(secret, data)`

### 其他可选算法
```java
// MD5
return DigestUtil.md5Hex(secret + data);

// SHA1
return DigestUtil.sha1Hex(secret + data);

// 自定义算法
// 需要与客户端的generateSignature方法保持一致
```

## 错误处理

### 常见错误码
- `SIGNATURE_VALIDATION_FAILED`：签名验证失败
- `INVALID_TIMESTAMP`：时间戳超出允许范围
- `DUPLICATE_REQUEST`：重复请求
- `INVALID_ACCESS_ID`：无效的AccessId

### 响应格式
```json
{
  "success": false,
  "message": "签名验证失败",
  "code": "SIGNATURE_VALIDATION_FAILED"
}
```

## 性能优化建议

1. **缓存策略**：
   - AppSecret使用Redis缓存
   - 设置合理的缓存过期时间

2. **数据库优化**：
   - 在access_id字段上建立索引
   - 定期清理过期的签名日志

3. **Redis优化**：
   - 使用Redis集群提高可用性
   - 设置合理的内存淘汰策略

## 监控和日志

### 关键指标
- 签名验证成功率
- 请求响应时间
- 异常请求频率

### 日志记录
- 验证失败的详细信息
- 可疑请求的IP和参数
- 系统异常和错误

## 注意事项

1. **时钟同步**：确保客户端和服务端时钟同步
2. **密钥安全**：AppSecret必须安全存储，定期轮换
3. **算法一致性**：客户端和服务端的签名算法必须完全一致
4. **参数编码**：确保参数编码方式一致（UTF-8）
5. **网络安全**：建议使用HTTPS传输