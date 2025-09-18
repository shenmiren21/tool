# Java 开发指南

## Lombok 注解

### @RequiredArgsConstructor
- **作用**：自动生成构造函数，为类中所有 `final` 字段和标记了 `@NonNull` 的字段生成一个构造函数
- **使用场景**：Spring 依赖注入、不可变对象创建、减少样板代码
- **优势**：代码简洁、依赖注入、不可变性、最佳实践
- **注意事项**：只为 `final` 和 `@NonNull` 字段生成构造函数参数

```java
@RequiredArgsConstructor
public class CityService {
    private final CityMapper cityMapper;
    private final UserService userService;
    
    // Lombok 自动生成构造函数
}
```

### 相关注解
- **@AllArgsConstructor**：为所有字段生成构造函数
- **@NoArgsConstructor**：生成无参构造函数
- **@Data**：包含 getter、setter、toString、equals、hashCode 等

## Spring 依赖注入

### final 字段声明
- **依赖声明**：声明对其他组件的依赖
- **不可变性**：`final` 关键字确保字段在初始化后不能被重新赋值
- **线程安全**：final 字段天然线程安全
- **Spring 注入目标**：Spring 容器会通过构造函数注入实例

```java
@Service
@RequiredArgsConstructor
public class CityService {
    private final CityMapper cityMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public City getCityById(Long id) {
        String cacheKey = "city:" + id;
        City city = (City) redisTemplate.opsForValue().get(cacheKey);
        
        if (city == null) {
            city = cityMapper.selectById(id);
            if (city != null) {
                redisTemplate.opsForValue().set(cacheKey, city, 30, TimeUnit.MINUTES);
            }
        }
        
        return city;
    }
}
```

### 依赖注入方式对比

#### 1. 构造函数注入（推荐）
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
}
```
- **优势**：依赖明确、易于测试、保证依赖不为 null、支持不可变对象、循环依赖检测

#### 2. 字段注入（不推荐）
```java
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;  // 不推荐
}
```
- **缺点**：难以进行单元测试、隐藏依赖关系、可能出现 null 值

#### 3. Setter 注入（特殊场景）
```java
@Service
public class UserService {
    private UserMapper userMapper;
    
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
}
```

### 最佳实践
- **优先使用构造函数注入**
- **使用 final 修饰依赖字段**
- **结合 @RequiredArgsConstructor 减少样板代码**
- **避免循环依赖**
- **保持依赖关系简单明确**

### 测试友好的设计
```java
@ExtendWith(MockitoExtension.class)
class CityServiceTest {
    
    @Mock
    private CityMapper cityMapper;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    private CityService cityService;
    
    @BeforeEach
    void setUp() {
        cityService = new CityService(cityMapper, redisTemplate);
    }
    
    @Test
    void testGetCityById() {
        // 测试代码...
    }
}