# ServiceInvokeUtils 工具类使用说明

## 概述

ServiceInvokeUtils 是一个通用的 Spring Service 方法调用和 JSON 转换工具类，提供了便捷的方式来动态调用 Service 方法并处理返回结果。支持泛型类型安全和多种数据类型转换。

**作者：** shenmiren21

## 主要功能

- 通过 Service 类型或 Bean 名称动态调用方法
- 自动将方法返回结果转换为键值对 Map
- **新增：** 支持泛型类型安全的方法调用
- **新增：** 直接返回指定类型的结果
- 支持 JSON 字符串解析和转换
- 提供类型安全的值获取方法
- 完善的异常处理和日志记录

## 核心方法

### 1. invokeServiceForMap - 通过类型调用（返回Map）

```java
public static <T> Map<String, Object> invokeServiceForMap(Class<T> serviceClass, String methodName, Object... args)
```

**参数说明：**
- `serviceClass`: Service 类的 Class 对象
- `methodName`: 要调用的方法名
- `args`: 方法参数（可变参数）

**返回值：** 包含方法返回结果的键值对 Map

### 2. invokeServiceForMap - 通过Bean名称调用（返回Map）

```java
public static Map<String, Object> invokeServiceForMap(String serviceBeanName, String methodName, Object... args)
```

**参数说明：**
- `serviceBeanName`: Spring 容器中的 Bean 名称
- `methodName`: 要调用的方法名
- `args`: 方法参数（可变参数）

**返回值：** 包含方法返回结果的键值对 Map

### 3. invokeServiceForType - 通过类型调用（返回指定类型）🆕

```java
public static <T, R> R invokeServiceForType(Class<T> serviceClass, String methodName, Class<R> returnType, Object... args)
```

**参数说明：**
- `serviceClass`: Service 类的 Class 对象
- `methodName`: 要调用的方法名
- `returnType`: 期望返回的类型 Class
- `args`: 方法参数（可变参数）

**返回值：** 指定类型的结果对象

### 4. invokeServiceForType - 通过Bean名称调用（返回指定类型）🆕

```java
public static <R> R invokeServiceForType(String serviceBeanName, String methodName, Class<R> returnType, Object... args)
```

**参数说明：**
- `serviceBeanName`: Spring 容器中的 Bean 名称
- `methodName`: 要调用的方法名
- `returnType`: 期望返回的类型 Class
- `args`: 方法参数（可变参数）

**返回值：** 指定类型的结果对象

## 使用示例

### 示例1：通过Service类型调用无参方法（返回Map）

```java
// 调用 UserService 的 getAllUsers 方法
Map<String, Object> result = ServiceInvokeUtils.invokeServiceForMap(
    UserService.class, 
    "getAllUsers"
);

// 获取结果
String status = ServiceInvokeUtils.getStringValue(result, "status");
Integer count = ServiceInvokeUtils.getIntValue(result, "count");
```

### 示例2：通过Bean名称调用带参方法（返回Map）

```java
// 调用 userService Bean 的 getUserById 方法
Map<String, Object> result = ServiceInvokeUtils.invokeServiceForMap(
    "userService", 
    "getUserById", 
    1001L
);

// 获取用户信息
String userName = ServiceInvokeUtils.getStringValue(result, "userName");
String email = ServiceInvokeUtils.getStringValue(result, "email");
Boolean isActive = ServiceInvokeUtils.getBooleanValue(result, "isActive");
```

### 示例3：调用复杂参数方法（返回Map）

```java
// 创建查询条件
UserQueryDTO queryDTO = new UserQueryDTO();
queryDTO.setAge(25);
queryDTO.setCity("北京");

// 调用查询方法
Map<String, Object> result = ServiceInvokeUtils.invokeServiceForMap(
    UserService.class, 
    "searchUsers", 
    queryDTO, 
    1, 
    10
);

// 获取分页结果
Integer total = ServiceInvokeUtils.getIntValue(result, "total");
Integer pageSize = ServiceInvokeUtils.getIntValue(result, "pageSize");
```

### 示例4：类型安全调用（返回指定类型）🆕

```java
// 直接获取用户对象
UserDTO user = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "getUserById", 
    UserDTO.class,
    1001L
);

// 直接获取用户列表
List<UserDTO> users = ServiceInvokeUtils.invokeServiceForType(
    "userService", 
    "getAllUsers", 
    new TypeReference<List<UserDTO>>(){}.getClass()
);

// 直接获取分页结果
PageResult<UserDTO> pageResult = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "searchUsers", 
    PageResult.class,
    queryDTO, 1, 10
);
```

### 示例5：泛型值获取🆕

```java
Map<String, Object> result = ServiceInvokeUtils.invokeServiceForMap(
    UserService.class, 
    "getUserInfo", 
    userId
);

// 使用泛型方法获取指定类型的值
String userName = ServiceInvokeUtils.getValue(result, "userName", String.class);
Integer age = ServiceInvokeUtils.getValue(result, "age", Integer.class);
Date createTime = ServiceInvokeUtils.getValue(result, "createTime", Date.class);
UserProfile profile = ServiceInvokeUtils.getValue(result, "profile", UserProfile.class);
```

## 辅助方法

### JSON转换方法

```java
// 直接调用Service方法获取JSON字符串
String jsonResult = ServiceInvokeUtils.invokeServiceForJson(serviceInstance, "methodName", args);

// 将JSON字符串转换为Map
Map<String, Object> map = ServiceInvokeUtils.convertJsonToMap(jsonResult);

// 🆕 将JSON字符串转换为指定类型
UserDTO user = ServiceInvokeUtils.convertJsonToType(jsonResult, UserDTO.class);
```

### 类型安全的值获取方法

```java
// 传统方法
String stringValue = ServiceInvokeUtils.getStringValue(resultMap, "key");
Integer intValue = ServiceInvokeUtils.getIntValue(resultMap, "key");
Boolean boolValue = ServiceInvokeUtils.getBooleanValue(resultMap, "key");

// 🆕 泛型方法（推荐）
String stringValue = ServiceInvokeUtils.getValue(resultMap, "key", String.class);
Integer intValue = ServiceInvokeUtils.getValue(resultMap, "key", Integer.class);
Boolean boolValue = ServiceInvokeUtils.getValue(resultMap, "key", Boolean.class);
CustomObject customObj = ServiceInvokeUtils.getValue(resultMap, "key", CustomObject.class);
```

## 注意事项

### 1. 依赖要求

- Spring Framework（用于Bean获取）
- Jackson（用于JSON处理）
- Lombok（用于日志）
- 需要 `SpringUtils` 和 `JacksonUtils` 工具类支持

### 2. 方法查找规则

工具类按以下顺序查找方法：
1. 精确参数类型匹配
2. 同名方法且参数数量匹配
3. 无参方法（当传入参数为空时）

### 3. 异常处理

- 所有异常都会被捕获并记录日志
- `invokeServiceForMap` 发生异常时返回空的 HashMap
- `invokeServiceForType` 发生异常时返回 null
- 建议检查返回结果是否为空

### 4. 性能考虑

- 使用反射调用，性能略低于直接调用
- 适合配置化、动态化场景
- 不建议在高频调用场景中使用

### 5. 类型转换规则🆕

`getValue` 方法支持以下类型转换：
- 基本类型：String, Integer, Long, Double, Boolean
- 复杂类型：通过JSON序列化/反序列化转换
- 如果类型匹配，直接返回
- 转换失败时返回 null 并记录警告日志

## 最佳实践

### 1. 错误处理

```java
// Map方式
Map<String, Object> result = ServiceInvokeUtils.invokeServiceForMap(
    UserService.class, 
    "getUserById", 
    userId
);

if (result.isEmpty()) {
    // 处理调用失败的情况
    log.error("调用UserService.getUserById失败");
    return;
}

// 类型安全方式（推荐）🆕
UserDTO user = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "getUserById", 
    UserDTO.class,
    userId
);

if (user == null) {
    // 处理调用失败的情况
    log.error("调用UserService.getUserById失败");
    return;
}
```

### 2. 参数验证

```java
// 在调用前验证必要参数
if (userId == null) {
    throw new IllegalArgumentException("用户ID不能为空");
}

UserDTO user = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "getUserById", 
    UserDTO.class,
    userId
);
```

### 3. 泛型使用建议🆕

```java
// 推荐：使用类型安全的方法
UserDTO user = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "getUserById", 
    UserDTO.class,
    userId
);

// 而不是：
Map<String, Object> result = ServiceInvokeUtils.invokeServiceForMap(
    UserService.class, 
    "getUserById", 
    userId
);
String userName = ServiceInvokeUtils.getStringValue(result, "userName");
```

### 4. 日志配置

建议将 `com.chestnut.common.utils.ServiceInvokeUtils` 的日志级别设置为 `INFO` 或 `DEBUG`，以便跟踪方法调用情况。

```yaml
logging:
  level:
    com.chestnut.common.utils.ServiceInvokeUtils: DEBUG
```

## 常见问题

### Q1: 为什么返回空Map或null？

可能的原因：
- Service Bean 不存在
- 方法名错误
- 参数类型不匹配
- 方法执行异常
- JSON解析失败

### Q2: 如何处理复杂返回类型？

**推荐使用新的泛型方法：**
```java
// 直接获取复杂类型
PageResult<UserDTO> result = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "searchUsers", 
    PageResult.class,
    queryDTO
);
```

### Q3: 支持哪些参数类型？

支持所有Java对象类型，工具类会自动获取参数的运行时类型进行方法匹配。

### Q4: 泛型类型转换失败怎么办？🆕

- 检查目标类型是否有无参构造函数
- 确保JSON格式正确
- 查看日志中的详细错误信息
- 对于复杂类型，确保所有字段都可序列化

### Q5: 如何处理泛型集合类型？🆕

```java
// 对于简单的List<String>等，可以直接使用
List<String> names = ServiceInvokeUtils.invokeServiceForType(
    UserService.class, 
    "getUserNames", 
    List.class
);

// 对于复杂的泛型类型，建议先获取JSON再手动转换
String jsonResult = ServiceInvokeUtils.invokeServiceForJson(service, "method", args);
List<UserDTO> users = JacksonUtils.fromJson(jsonResult, new TypeReference<List<UserDTO>>(){});
```

## 更新日志

- **v1.0**: 初始版本，支持基本的Service方法调用和JSON转换
- **v1.1**: 优化代码结构，增强参数验证和错误处理
- **v1.2**: 添加类型安全的值获取方法，提升性能
- **v2.0**: 🆕 **重大更新**
  - 新增泛型支持的 `invokeServiceForType` 方法
  - 新增 `convertJsonToType` 泛型JSON转换方法
  - 新增 `getValue` 泛型值获取方法
  - 增强类型安全性和代码可读性
  - 更新作者信息为 shenmiren21
  - 优化错误处理和日志记录