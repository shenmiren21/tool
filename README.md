# 开发工具集合 (Development Tools Collection)

一个集成了多种开发和运维工具的实用工具库，包含Java服务工具、Linux部署脚本、前端工具库和Windows系统工具等。

## 📁 项目结构

```
tool/
├── java_service_utils/          # Java服务工具类
├── knowledge-docs/              # 编程小知识文档库
│   ├── java/                    # Java相关知识
│   └── tools/                   # 工具类使用指南
├── linux_jar_deploy_ssh/        # Linux JAR部署脚本
├── tool_js/                     # 前端JavaScript工具库
├── win_process_killer_bat/       # Windows进程和环境变量管理工具
├── sys_city.sql                 # 城市数据SQL文件
└── README.md                    # 项目说明文档
```

## 🛠️ 工具介绍

### 1. Java服务工具类 (java_service_utils)

**功能：** 提供Spring Service方法动态调用和JSON转换功能

**主要特性：**
- 通过反射动态调用Spring Service方法
- 自动类型转换和JSON处理
- 支持泛型类型安全
- 完善的异常处理和日志记录
- 智能参数类型匹配
- **已优化**：使用Hutool工具类替代自定义工具，提升性能和稳定性

**核心文件：**
- `SpringServiceCaller.java` - 主要工具类（已优化）
- 使用 `cn.hutool.core.util.StrUtil` - 字符串工具
- 使用 `cn.hutool.json.JSONUtil` - JSON处理工具

**使用示例：**
```java
// 动态调用Service方法
UserDTO user = SpringServiceCaller.invokeService(
    UserDTO.class,
    UserService.class,
    "getUserById",
    1001L
);

// JSON转Map
Map<String, Object> map = SpringServiceCaller.convertJsonToMap(jsonString);
```

### 2. 编程小知识文档库 (knowledge-docs)

**功能：** 收集和整理日常开发中的编程小知识、技巧和最佳实践

**主要特性：**
- **Java 开发指南**：Lombok注解、Spring依赖注入最佳实践
- **工具类使用**：Hutool工具类详细使用指南
- **实例驱动**：每个知识点都包含完整的代码示例
- **快速查找**：支持IDE全局搜索，便于定位相关知识点
- **持续更新**：根据实际项目经验不断完善内容

**文档结构：**
- `java/java-development-guide.md` - Java开发指南（Lombok + Spring）
- `tools/hutool-usage.md` - Hutool工具类使用指南
- `README.md` - 文档库使用说明和导航

**知识点概览：**
```
📚 Java开发指南
├── @RequiredArgsConstructor 自动构造函数生成
├── final字段声明和依赖注入最佳实践
└── 实际项目应用场景和代码示例

🛠️ Hutool工具类
├── StrUtil - 字符串处理工具
├── JSONUtil - JSON转换工具
├── CollUtil - 集合操作工具
├── DateUtil - 日期时间工具
└── FileUtil - 文件操作工具
```

### 3. Linux JAR部署脚本 (linux_jar_deploy_ssh)

**功能：** Java服务一体化管理脚本，支持启动、停止、重启等操作

**主要特性：**
- 启动/停止/重启/状态查看/重载
- 前台调试模式
- systemd单元自动安装
- logrotate日志切割
- 并发保护和SIGUSR2探测
- 启动失败日志回显

**使用方法：**
```bash
# 基本操作
./service.sh start                    # 启动服务
./service.sh stop                     # 停止服务
./service.sh restart                  # 重启服务
./service.sh status                   # 查看状态

# 指定JAR文件
./service.sh start -f /path/to/app.jar

# 带日志输出
./service.sh start -f /path/to/app.jar 1>stdout.log 2>stderr.log
```

### 4. 前端JavaScript工具库 (tool_js)

**功能：** 前端开发常用的JavaScript工具函数库

**主要特性：**
- 文件预览功能（支持Office、图片、视频等格式）
- Ajax提交和表单验证
- DOM操作辅助函数
- URL和主机地址处理
- 通用路径替换逻辑

**核心功能：**
- **文件预览：** 支持doc/docx/xls/xlsx/ppt/pptx/pdf等Office文件
- **图片预览：** 支持jpg/jpeg/png/gif/bmp等图片格式
- **视频预览：** 支持mp4/avi/mov/mkv等视频格式
- **DOM操作：** 提供便捷的元素选择器

**使用示例：**
```javascript
// 文件预览
$.file._view('/path/to/file.pdf', 'old-segment', 'new-segment');

// DOM操作
var element = $._id('elementId');
var elements = $._name('elementName');

// 获取主机地址
var host = $.u._host();
```

### 5. Windows系统工具 (win_process_killer_bat)

**功能：** Windows系统进程管理和环境变量设置工具

#### 4.1 端口进程终止工具 (kill-ports.bat)

**功能：** 根据端口号查找并终止占用进程

**使用方法：**
```cmd
# 运行脚本
kill-ports.bat

# 输入端口号（支持多个端口，空格分隔）
端口: 8080 3000 9000
```

**特性：**
- 支持同时处理多个端口
- 自动查找端口占用的进程PID
- 强制终止进程
- 友好的用户交互界面

#### 4.2 环境变量设置工具 (env-var-setter.bat)

**功能：** 通用的Windows系统环境变量设置脚本

**特性：**
- 自动检查和申请管理员权限
- 支持UTF-8编码，正确显示中文
- 用户友好的交互界面
- 安全的权限提升机制

**使用方法：**
```cmd
# 以管理员身份运行
env-var-setter.bat

# 按提示输入变量名和路径值
```

## 🚀 快速开始

### 环境要求

**Java工具类：**
- Java 8+
- Spring Framework
- Jackson库
- Maven/Gradle构建工具

**Linux部署脚本：**
- Linux操作系统
- Java运行环境
- Bash shell

**前端工具库：**
- 现代浏览器
- jQuery库

**Windows工具：**
- Windows操作系统
- 管理员权限（环境变量设置）

### 安装使用

1. **克隆项目**
```bash
git clone <repository-url>
cd tool
```

2. **Java工具类使用**
```bash
# 复制Java文件到项目中
cp java_service_utils/*.java /your/project/src/main/java/utils/

# 添加Hutool依赖到pom.xml
# <dependency>
#     <groupId>cn.hutool</groupId>
#     <artifactId>hutool-all</artifactId>
#     <version>5.8.22</version>
# </dependency>
```

3. **Linux脚本使用**
```bash
# 复制脚本到服务器
scp linux_jar_deploy_ssh/service.sh user@server:/path/to/jar/

# 设置执行权限
chmod +x service.sh
```

4. **前端工具使用**
```html
<!-- 引入jQuery和工具库 -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="tool_js/haedu.js"></script>
```

5. **Windows工具使用**
```cmd
# 直接运行bat文件
win_process_killer_bat\kill-ports.bat
win_process_killer_bat\env-var-setter.bat
```

## 📖 详细文档

- [Java服务工具类详细说明](java_service_utils/README.md)
- [编程小知识文档库](knowledge-docs/README.md)
- [Linux部署脚本使用手册](linux_jar_deploy_ssh/README.md)

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个工具集合。

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 👨‍💻 作者

- **shenmiren21** - Java服务工具类开发
- 其他贡献者欢迎在此添加信息

## 🔄 更新日志

- **v2.1** - 工具优化和文档完善
  - **Java工具类优化**：使用Hutool替代自定义工具类，提升性能和稳定性
  - **新增知识文档库**：添加knowledge-docs编程小知识文档库
  - **文档结构优化**：完善README结构，添加详细的使用指南
  - **项目结构更新**：修正文件夹命名，反映实际项目结构

- **v2.0** - 重构项目结构，统一命名规范
  - 将ServiceInvokeUtils重命名为java_service_utils
  - 优化README文档结构
  - 完善各工具的使用说明

- **v1.0** - 初始版本
  - 集成Java服务工具类
  - 添加Linux部署脚本
  - 包含前端工具库
  - 提供Windows系统工具