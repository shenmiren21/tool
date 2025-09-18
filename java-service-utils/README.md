# ServiceInvokeUtils 工具类使用说明 (Hutool优化版)

## 概述

ServiceInvokeUtils 是一个通用的 Spring Service 方法调用和 JSON 转换工具类，提供了便捷的方式来动态调用 Service 方法并处理返回结果。支持泛型类型安全和多种数据类型转换。

**版本：** 2.0.0 (Hutool优化版)  
**作者：** shenmiren21

## 🚀 v2.0.0 优化亮点

- ✅ **依赖简化**：使用 Hutool 5.8.37 替换多个工具库依赖
- ✅ **代码精简**：移除自定义 JacksonUtils、StringUtils、JacksonException 类
- ✅ **性能提升**：Hutool 工具类经过高度优化，性能更佳
- ✅ **维护性增强**：减少自定义代码，降低维护成本
- ✅ **功能保持**：所有原有功能完全保持，API 无变化

## 主要功能

- 通过 Service 类型动态调用方法
- 自动将方法返回结果转换为指定类型
- 支持泛型类型安全的方法调用
- 支持 JSON 字符串解析和转换
- 提供类型安全的值获取方法
- 完善的异常处理和日志记录
- 智能参数类型匹配和兼容性检查

## 依赖配置

### Maven 依赖

```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.37</version>
</dependency>

<!-- Spring Context (provided) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.3.21</version>
    <scope>provided</scope>
</dependency>

<!-- Spring Core (provided) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>5.3.21</version>
    <scope>provided</scope>
</dependency>

<!-- Lombok (provided) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.24</version>
    <scope>provided</scope>
</dependency>

<!-- SLF4J API (provided) -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.36</version>
    <scope>provided</scope>
</dependency>
```

### 替换的依赖

使用 Hutool 后，以下依赖不再需要：

```xml
<!-- 不再需要的依赖 -->
<!--
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-guava</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
</dependency>
-->
```

## 使用示例

### 1. 基本方法调用

```java
// 调用 UserService 的 getUserById 方法，返回 User 对象
User user = ServiceInvokeUtils.invokeService(
    User.class, 
    UserService.class, 
    "getUserById", 
    1L
);
```

### 2. 返回 List 类型

```java
// 调用 UserService 的 getAllUsers 方法，返回 List<User>
List<User> users = ServiceInvokeUtils.invokeService(
    List.class, 
    UserService.class, 
    "getAllUsers"
);
```

### 3. 返回 Map 类型

```java
// 调用 UserService 的 getUserInfo 方法，返回 Map
Map<String, Object> userInfo = ServiceInvokeUtils.invokeService(
    Map.class, 
    UserService.class, 
    "getUserInfo", 
    1L
);
```

### 4. JSON 字符串转换

```java
// 将 JSON 字符串转换为 Map
String jsonStr = "{\"name\":\"张三\",\"age\":25}";
Map<String, Object> map = ServiceInvokeUtils.convertJsonToMap(jsonStr);

// 从 Map 中获取值
String name = ServiceInvokeUtils.getStringValue(map, "name");
Integer age = ServiceInvokeUtils.getIntValue(map, "age");
```

### 5. 类型安全的值获取

```java
Map<String, Object> data = new HashMap<>();
data.put("id", 1L);
data.put("name", "张三");
data.put("active", true);

// 类型安全的值获取
Long id = ServiceInvokeUtils.getLongValue(data, "id");
String name = ServiceInvokeUtils.getStringValue(data, "name");
Boolean active = ServiceInvokeUtils.getBooleanValue(data, "active");

// 泛型方法获取值
User user = ServiceInvokeUtils.getValue(data, "user", User.class);
```

## 核心方法说明

### invokeService()

```java
public static <T, R> T invokeService(Class<T> returnType, Class<R> serviceClass, String methodName, Object... args)
```

- **returnType**: 期望的返回值类型
- **serviceClass**: Service 类的 Class 对象
- **methodName**: 要调用的方法名
- **args**: 方法参数（可变参数）

### convertJsonToMap()

```java
public static Map<String, Object> convertJsonToMap(String jsonString)
```

将 JSON 字符串转换为 Map 对象。

### 值获取方法

- `getStringValue(Map<String, Object> map, String key)`: 获取字符串值
- `getIntValue(Map<String, Object> map, String key)`: 获取整数值
- `getLongValue(Map<String, Object> map, String key)`: 获取长整数值
- `getBooleanValue(Map<String, Object> map, String key)`: 获取布尔值
- `getValue(Map<String, Object> map, String key, Class<T> targetType)`: 泛型值获取

## 技术特性

### 智能类型转换

工具类支持以下类型的智能转换：

- 基本数据类型及其包装类
- 字符串类型
- 集合类型（List、Set、Map）
- 自定义对象类型
- JSON 字符串与对象的相互转换

### 参数类型匹配

支持灵活的参数类型匹配：

- 精确类型匹配
- 继承关系匹配
- 基本类型与包装类型的兼容性匹配
- 数组类型匹配

### 异常处理

- 完善的异常捕获和日志记录
- 友好的错误信息提示
- 优雅的降级处理

## 注意事项

1. **Spring 环境**：需要在 Spring 容器环境中使用，依赖 SpringUtils 获取 Bean 实例
2. **方法可见性**：被调用的 Service 方法需要是 public 的
3. **参数类型**：传入的参数类型需要与目标方法的参数类型兼容
4. **返回值处理**：如果方法返回 null，工具类也会返回 null
5. **日志级别**：建议将日志级别设置为 INFO 以上，避免过多的调试信息

## 性能优化

- 使用 Hutool 高性能 JSON 处理
- 方法查找结果缓存（如需要可自行实现）
- 减少不必要的对象创建
- 优化的类型转换逻辑

## 版本历史

### v2.0.0 (2024-01-XX)
- 使用 Hutool 5.8.37 替换多个工具库依赖
- 移除自定义 JacksonUtils、StringUtils、JacksonException 类
- 优化代码结构，提升性能
- 保持 API 兼容性

### v1.0.0 (2023-XX-XX)
- 初始版本
- 基于 Jackson、Commons Lang3 等库实现

## 许可证

Apache License 2.0

## 联系方式

- 作者：shenmiren21
- 邮箱：2772734342@qq.com

---

**注意**：升级到 v2.0.0 版本时，只需要更新 Maven 依赖配置，无需修改任何业务代码。