Java Service 一体化管理脚本使用手册

> 单文件即全功能：`service.sh` 内已集成  
>
> - 启动/停止/重启/状态/重载  
> - 前台调试模式  
> - systemd 单元自动安装  
> - logrotate 日志切割  
> - 并发保护、SIGUSR2 探测、启动失败日志回显等增强特性  

---

1. 准备

2. 把脚本放到 jar 所在目录（或任意目录均可）。
3. 赋可执行权限  

```bash
   chmod +x service.sh
```

4.设置JAVA_HOME环境

```shell
# 1. 检查JAVA_HOME
echo $JAVA_HOME
# 2. 检查Java版本
java -version
# 3. 检查JAVA_HOME中的Java
$JAVA_HOME/bin/java -version


#立即修正JAVA_HOME
# 使用JRE路径（确保有效）
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.412
# 测试
$JAVA_HOME/bin/java -version
# 测试能否直接运行JAR
java -jar /home/project/ysbjk/haedu-admin.jar --help
```

---

2.1 使用方法

```bash
# 使用默认JAR文件启动
./service.sh start

# 指定JAR文件启动
./service.sh start -f /home/project/ysbjk/haedu-admin.jar
# 指定启动 并生成状态日志与错误日志文件
./service.sh start -f /home/project/ysbjk/haedu-admin.jar 1>stdout.log 2>stderr.log

# 查看状态
./service.sh status

# 停止服务
./service.sh stop

# 重启服务
./service.sh restart

# 查看帮助
./service.sh help

# 服务启动验证
ps aux | grep haedu-admin（项目名）
```

2.2 查看应用日志

```shell
# 查看启动日志
tail -f /home/project/ysbjk/logs/haedu-admin.log

# 或者查看运行日志
tail -f /home/project/ysbjk/run.log
```
