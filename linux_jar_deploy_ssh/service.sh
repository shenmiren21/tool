#!/bin/bash

# 环境初始化 - 增加错误处理
. /etc/profile &>/dev/null
. /etc/rc.d/init.d/functions 2>/dev/null || {
    # 如果系统函数库不存在，定义基本的action函数
    action() {
        echo "$1"
        $2 && echo "[  OK  ]" || echo "[FAILED]"
    }
}

# 可选的内存配置脚本
[ -f /server/scripts/get_project_mem.sh ] && . /server/scripts/get_project_mem.sh &>/dev/null

# 动态检测Java路径
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    java="$JAVA_HOME/bin/java"
elif [ -x "/opt/primeton/jdk1.8.0_401/bin/java" ]; then
    java="/opt/primeton/jdk1.8.0_401/bin/java"
elif command -v java >/dev/null 2>&1; then
    java="java"
else
    echo "错误: 未找到Java环境，请设置JAVA_HOME或安装Java"
    exit 1
fi

# 目录和项目配置
Dir=$(cd "$(dirname "$0")"; pwd)
project=$(basename "$Dir")

# 支持命令行参数指定JAR文件
jar_file=""
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--file)
            jar_file="$2"
            shift 2
            ;;
        start|stop|status|restart)
            action="$1"
            shift
            ;;
        *)
            shift
            ;;
    esac
done

# 确定JAR文件路径
if [ -n "$jar_file" ]; then
    if [ ! -f "$jar_file" ]; then
        echo "错误: 指定的JAR文件不存在: $jar_file"
        exit 1
    fi
    project=$(basename "$jar_file" .jar)
else
    jar_file="$Dir/${project}.jar"
    if [ ! -f "$jar_file" ]; then
        echo "错误: 没有找到${project}.jar，请使用 -f 参数指定JAR文件"
        exit 1
    fi
fi

# 内存配置优化
get_mem "$project" &>/dev/null 2>&1 || true
if [ -z "$Mem" ]; then
    Mem="512"
else
    if ! [[ "$Mem" =~ ^[0-9]+$ ]]; then
        echo "错误: 内存参数必须是数字，当前值: $Mem"
        exit 1
    fi
fi

# PID目录配置
pidDir="$(dirname "$Dir")/pid"
[ -d "$pidDir" ] || mkdir -p "$pidDir"
pid_file="$pidDir/${project}.pid"

# 日志配置
log_file="$Dir/logs/${project}.log"
[ -d "$Dir/logs" ] || mkdir -p "$Dir/logs"

# 状态检查函数
status_service() {
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file" 2>/dev/null)
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo "服务正在运行 (PID: $pid)"
            ps aux | grep "$jar_file" | grep -v grep
            return 0
        else
            echo "PID文件存在但进程未运行，清理PID文件"
            rm -f "$pid_file"
        fi
    fi
    echo "服务未运行"
    return 1
}

# 启动服务函数
start_service() {
    # 检查服务是否已运行
    if status_service >/dev/null 2>&1; then
        echo "服务已经在运行中"
        return 0
    fi
    
    echo "正在启动服务: $project"
    echo "JAR文件: $jar_file"
    echo "Java路径: $java"
    echo "内存配置: ${Mem}MB"
    
    # JVM参数优化
    JAVA_OPTS="-Xms${Mem}m -Xmx${Mem}m"
    JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
    JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
    JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=$Dir/logs/"
    JAVA_OPTS="$JAVA_OPTS -Duser.timezone=Asia/Shanghai"
    JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
    JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true"
    
    # 启动应用
    nohup "$java" $JAVA_OPTS -server -jar "$jar_file" > "$log_file" 2>&1 &
    local start_pid=$!
    
    # 保存PID
    echo $start_pid > "$pid_file"
    
    # 等待启动
    sleep 3
    
    # 验证启动结果
    if kill -0 $start_pid 2>/dev/null; then
        action "服务启动成功 (PID: $start_pid)" /bin/true
        echo "日志文件: $log_file"
    else
        action "服务启动失败" /bin/false
        echo "请查看日志: $log_file"
        rm -f "$pid_file"
        return 1
    fi
}

# 停止服务函数
stop_service() {
    if [ ! -f "$pid_file" ]; then
        action "服务已经停止" /bin/true
        return 0
    fi
    
    local pid=$(cat "$pid_file" 2>/dev/null)
    if [ -z "$pid" ]; then
        echo "PID文件为空，清理文件"
        rm -f "$pid_file"
        return 0
    fi
    
    echo "正在停止服务 (PID: $pid)..."
    
    # 优雅停止
    if kill -0 "$pid" 2>/dev/null; then
        kill -TERM "$pid" 2>/dev/null
        
        # 等待优雅停止
        local count=0
        while [ $count -lt 10 ] && kill -0 "$pid" 2>/dev/null; do
            sleep 1
            count=$((count + 1))
        done
        
        # 强制停止
        if kill -0 "$pid" 2>/dev/null; then
            echo "优雅停止超时，强制终止进程"
            kill -9 "$pid" 2>/dev/null
            sleep 1
        fi
    fi
    
    # 清理PID文件
    rm -f "$pid_file"
    action "服务停止成功" /bin/true
}

# 重启服务函数
restart_service() {
    echo "正在重启服务..."
    stop_service
    sleep 2
    start_service
}

# 显示帮助信息
show_help() {
    echo "用法: $0 {start|stop|restart|status} [-f JAR文件路径]"
    echo ""
    echo "命令:"
    echo "  start    启动服务"
    echo "  stop     停止服务"
    echo "  restart  重启服务"
    echo "  status   查看服务状态"
    echo ""
    echo "选项:"
    echo "  -f, --file   指定JAR文件路径"
    echo ""
    echo "示例:"
    echo "  $0 start"
    echo "  $0 start -f /path/to/app.jar"
    echo "  $0 status"
}

# 主函数
main() {
    case "${action:-$1}" in
        start)
            start_service
            ;;
        stop)
            stop_service
            ;;
        status)
            status_service
            ;;
        restart)
            restart_service
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo "错误: 未知命令 '$1'"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"