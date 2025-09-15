# ServiceInvokeUtils 工具类使用说明

## 概述

ServiceInvokeUtils 是一个通用的 Spring Service 方法调用和 JSON 转换工具类，提供了便捷的方式来动态调用 Service 方法并处理返回结果。支持泛型类型安全和多种数据类型转换。

**作者：** shenmiren21

## 主要功能

- 通过 Service 类型动态调用方法
- 自动将方法返回结果转换为指定类型
- 支持泛型类型安全的方法调用
- 支持 JSON 字符串解析和转换
- 提供类型安全的值获取方法
- 完善的异常处理和日志记录
- 智能参数类型匹配和兼容性检查

## 安装

1. 确保项目中已引入 ServiceInvokeUtils 工具类库
2. 确保项目中已引入 Jackson 库（用于 JSON 转换）
3. 确保项目中已引入 Spring 框架（用于动态代理）

## 配置

```xml

        <!-- Jackson -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>2.15.3</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.3</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-guava</artifactId>
            <version>2.15.3</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.8.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-compress</artifactId>
            <version>1.21</version>
        </dependency>


```

## 核心方法

### 1. invokeService - 通用方法调用（返回指定类型）

```java
public static <T, R> T invokeService(Class<T> returnType, Class<R> serviceClass, String methodName, Object... args)
```

**参数说明：**
- `returnType`: 期望返回的类型 Class
- `serviceClass`: Service 类的 Class 对象
- `methodName`: 要调用的方法名
- `args`: 方法参数（可变参数）

**返回值：** 指定类型的结果对象

### 2. convertJsonToMap - JSON转Map

```java
public static Map<String, Object> convertJsonToMap(String jsonString)
```

**参数说明：**
- `jsonString`: JSON字符串

**返回值：** 包含JSON数据的Map对象

## 使用示例

### 示例1：调用Service方法获取用户信息

```java
// 直接获取用户对象
UserDTO user = ServiceInvokeUtils.invokeService(
    UserDTO.class,           // 返回类型
    UserService.class,       // Service类
    "getUserById",          // 方法名
    1001L                    // 参数
);

if (user != null) {
    System.out.println("用户名: " + user.getUserName());
    System.out.println("邮箱: " + user.getEmail());
} else {
    System.out.println("获取用户信息失败");
}
```

### 示例2：调用Service方法获取用户列表

```java
// 获取用户列表（需要使用TypeReference处理泛型）
String jsonResult = ServiceInvokeUtils.invokeService(
    String.class,            // 先获取JSON字符串
    UserService.class, 
    "getAllUsers"
);

// 然后转换为具体类型
List<UserDTO> users = JacksonUtils.fromList(jsonResult, UserDTO.class);

if (users != null && !users.isEmpty()) {
    users.forEach(u -> System.out.println("用户: " + u.getUserName()));
}
```

### 示例3：调用复杂参数方法

```java
// 创建查询条件
UserQueryDTO queryDTO = new UserQueryDTO();
queryDTO.setAge(25);
queryDTO.setCity("北京");

// 调用查询方法获取分页结果
PageResult<UserDTO> pageResult = ServiceInvokeUtils.invokeService(
    PageResult.class,        // 返回类型
    UserService.class, 
    "searchUsers", 
    queryDTO,               // 查询条件
    1,                      // 页码
    10                      // 页大小
);

if (pageResult != null) {
    System.out.println("总数: " + pageResult.getTotal());
    System.out.println("当前页数据: " + pageResult.getRecords().size());
}
```

### 示例4：JSON字符串处理

```java
// 获取JSON字符串结果
String jsonResult = ServiceInvokeUtils.invokeService(
    String.class,
    UserService.class,
    "getUserInfo",
    userId
);

// 转换为Map进行处理
Map<String, Object> resultMap = ServiceInvokeUtils.convertJsonToMap(jsonResult);

// 使用类型安全的值获取方法
String userName = ServiceInvokeUtils.getStringValue(resultMap, "userName");
Integer age = ServiceInvokeUtils.getIntValue(resultMap, "age");
Boolean isActive = ServiceInvokeUtils.getBooleanValue(resultMap, "isActive");

// 使用泛型方法获取值（推荐）
String email = ServiceInvokeUtils.getValue(resultMap, "email", String.class);
Date createTime = ServiceInvokeUtils.getValue(resultMap, "createTime", Date.class);
```

### 示例5：错误处理最佳实践

```java
try {
    UserDTO user = ServiceInvokeUtils.invokeService(
        UserDTO.class,
        UserService.class,
        "getUserById",
        userId
    );
    
    if (user == null) {
        log.warn("用户不存在或调用失败: {}", userId);
        return ResponseResult.error("用户不存在");
    }
    
    return ResponseResult.success(user);
    
} catch (Exception e) {
    log.error("调用用户服务异常: {}", e.getMessage(), e);
    return ResponseResult.error("系统异常");
}
```

## 辅助方法

### 类型安全的值获取方法

```java
// 基础类型获取
String stringValue = ServiceInvokeUtils.getStringValue(resultMap, "key");
Integer intValue = ServiceInvokeUtils.getIntValue(resultMap, "key");
Boolean boolValue = ServiceInvokeUtils.getBooleanValue(resultMap, "key");

// 泛型方法（推荐使用）
String stringValue = ServiceInvokeUtils.getValue(resultMap, "key", String.class);
Integer intValue = ServiceInvokeUtils.getValue(resultMap, "key", Integer.class);
Boolean boolValue = ServiceInvokeUtils.getValue(resultMap, "key", Boolean.class);
CustomObject customObj = ServiceInvokeUtils.getValue(resultMap, "key", CustomObject.class);
```

## 技术特性

### 1. 智能方法查找

工具类支持以下方法查找策略：
1. **精确参数类型匹配**：优先查找参数类型完全匹配的方法
2. **参数类型兼容性检查**：支持继承关系和基本类型与包装类型的兼容
3. **无参方法回退**：当传入参数为空时，尝试查找无参方法

### 2. 类型转换支持

- **基本类型转换**：String, Integer, Long, Boolean等
- **复杂对象转换**：通过JSON序列化/反序列化实现
- **集合类型处理**：支持List、Set、Map等集合类型
- **自动类型推断**：根据目标类型自动选择转换策略

### 3. 异常处理机制

- **完善的日志记录**：详细记录方法调用过程和异常信息
- **优雅的异常处理**：异常时返回null而不是抛出异常
- **参数验证**：自动验证必要参数的有效性

## 注意事项

### 1. 依赖要求

```xml
<!-- Spring Framework -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>

<!-- Jackson -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

需要配套的工具类：
- `SpringUtils`：用于获取Spring Bean
- `JacksonUtils`：用于JSON处理

### 2. 性能考虑

- 使用反射调用，性能略低于直接调用
- 适合配置化、动态化场景
- 不建议在高频调用场景中使用
- 建议在业务逻辑层使用，避免在数据访问层频繁调用

### 3. 类型安全建议

```java
// 推荐：明确指定返回类型
UserDTO user = ServiceInvokeUtils.invokeService(
    UserDTO.class,
    UserService.class,
    "getUserById",
    userId
);

// 不推荐：使用Object类型
Object result = ServiceInvokeUtils.invokeService(
    Object.class,
    UserService.class,
    "getUserById",
    userId
);
```

### 4. 日志配置建议

```yaml
logging:
  level:
    com.chestnut.api.utils.ServiceInvokeUtils: INFO
    # 开发环境可以设置为DEBUG查看详细调用信息
    # com.chestnut.api.utils.ServiceInvokeUtils: DEBUG
```

## 常见问题

### Q1: 为什么返回null？

可能的原因：
- Service Bean 不存在或未正确注册
- 方法名拼写错误
- 参数类型不匹配
- 方法执行过程中抛出异常
- JSON转换失败

**解决方案：**
1. 检查Service是否正确注册到Spring容器
2. 确认方法名和参数类型
3. 查看日志中的详细错误信息
4. 使用DEBUG级别日志跟踪调用过程

### Q2: 如何处理泛型集合类型？

```java
// 方法1：先获取JSON字符串，再转换
String jsonResult = ServiceInvokeUtils.invokeService(
    String.class,
    UserService.class,
    "getUserList"
);
List<UserDTO> users = JacksonUtils.fromList(jsonResult, UserDTO.class);

// 方法2：使用TypeReference
String jsonResult = ServiceInvokeUtils.invokeService(
    String.class,
    UserService.class,
    "getUserList"
);
List<UserDTO> users = JacksonUtils.from(jsonResult, new TypeReference<List<UserDTO>>(){});
```

### Q3: 参数类型匹配规则是什么？

工具类按以下优先级匹配方法：
1. 精确类型匹配
2. 继承关系匹配（子类可以赋值给父类）
3. 基本类型与包装类型互相兼容
4. 参数数量匹配的同名方法

### Q4: 如何调试方法调用问题？

```java
// 1. 开启DEBUG日志
// 2. 检查方法是否存在
Method[] methods = UserService.class.getMethods();
for (Method method : methods) {
    if ("getUserById".equals(method.getName())) {
        System.out.println("找到方法: " + method);
        System.out.println("参数类型: " + Arrays.toString(method.getParameterTypes()));
    }
}

// 3. 验证Service Bean
UserService userService = SpringUtils.getBean(UserService.class);
System.out.println("Service实例: " + userService);
```

## 更新日志

- **v1.0**: 初始版本，支持基本的Service方法调用和JSON转换
- **v2.0**: 重构核心架构
  - 简化API设计，统一为`invokeService`方法
  - 增强参数类型兼容性检查
  - 优化异常处理和日志记录
  - 改进JSON转换逻辑
  - 增加智能方法查找机制
  - 更新作者信息为 shenmiren21