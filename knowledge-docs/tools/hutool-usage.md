# Hutool 工具类使用指南

## 简介

Hutool 是一个小而全的 Java 工具类库，通过静态方法封装，降低相关 API 的学习成本，提高工作效率。

## 常用工具类

### 1. 字符串工具 - StrUtil

```java
import cn.hutool.core.util.StrUtil;

// 判空检查
if (StrUtil.isEmpty(str)) {
    // 字符串为空或null
}

if (StrUtil.isNotEmpty(str)) {
    // 字符串不为空
}

// 字符串格式化
String result = StrUtil.format("Hello {}, today is {}", "World", "Monday");
// 结果: "Hello World, today is Monday"

// 驼峰转换
String camelCase = StrUtil.toCamelCase("user_name"); // userName
String underlineCase = StrUtil.toUnderlineCase("userName"); // user_name
```

### 2. JSON 工具 - JSONUtil

```java
import cn.hutool.json.JSONUtil;

// 对象转 JSON 字符串
User user = new User("张三", 25);
String jsonStr = JSONUtil.toJsonStr(user);

// JSON 字符串转对象
User userFromJson = JSONUtil.toBean(jsonStr, User.class);

// JSON 字符串转 Map
Map<String, Object> map = JSONUtil.toBean(jsonStr, Map.class);

// 格式化 JSON（美化输出）
String prettyJson = JSONUtil.toJsonPrettyStr(user);
```

### 3. 集合工具 - CollUtil

```java
import cn.hutool.core.collection.CollUtil;

// 判空检查
if (CollUtil.isEmpty(list)) {
    // 集合为空
}

// 创建集合
List<String> list = CollUtil.newArrayList("a", "b", "c");
Set<String> set = CollUtil.newHashSet("x", "y", "z");

// 集合转换
String[] array = CollUtil.toArray(list, String.class);
List<String> listFromArray = CollUtil.newArrayList(array);
```

### 4. 日期工具 - DateUtil

```java
import cn.hutool.core.date.DateUtil;

// 当前时间
Date now = DateUtil.date();
String nowStr = DateUtil.now(); // yyyy-MM-dd HH:mm:ss

// 日期格式化
String dateStr = DateUtil.format(new Date(), "yyyy-MM-dd");

// 字符串转日期
Date date = DateUtil.parse("2024-01-01", "yyyy-MM-dd");

// 日期计算
Date tomorrow = DateUtil.offsetDay(new Date(), 1);
Date lastWeek = DateUtil.offsetWeek(new Date(), -1);
```

### 5. 文件工具 - FileUtil

```java
import cn.hutool.core.io.FileUtil;

// 读取文件
String content = FileUtil.readUtf8String("config.txt");
List<String> lines = FileUtil.readUtf8Lines("data.txt");

// 写入文件
FileUtil.writeUtf8String("Hello World", "output.txt");

// 文件操作
boolean exists = FileUtil.exist("test.txt");
boolean deleted = FileUtil.del("temp.txt");
FileUtil.copy("source.txt", "target.txt", true);
```

## 项目中的应用示例

### 替换自定义工具类

**原代码（使用自定义工具类）：**
```java
// 使用自定义 StringUtils
if (StringUtils.isEmpty(key)) {
    return null;
}

// 使用自定义 JacksonUtils
String json = JacksonUtils.to(object);
Object result = JacksonUtils.from(json, targetType);
```

**优化后（使用 Hutool）：**
```java
// 使用 Hutool StrUtil
if (StrUtil.isEmpty(key)) {
    return null;
}

// 使用 Hutool JSONUtil
String json = JSONUtil.toJsonStr(object);
Object result = JSONUtil.toBean(json, targetType);
```

### Maven 依赖

```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.37</version>
</dependency>
```

## 优势

1. **功能全面**：涵盖字符串、日期、集合、IO、加密等常用功能
2. **API 简洁**：方法命名直观，易于理解和使用
3. **性能优化**：经过大量项目验证，性能稳定
4. **文档完善**：官方文档详细，社区活跃
5. **持续更新**：版本更新频繁，bug 修复及时

## 注意事项

- 建议使用最新稳定版本
- 大型项目可考虑按需引入具体模块而非 hutool-all
- 注意与项目中其他 JSON 库的兼容性