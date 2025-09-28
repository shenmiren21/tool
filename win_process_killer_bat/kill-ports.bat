@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo 请输入要关闭的端口号（多个端口用空格分隔）:
set /p ports=端口: 

for %%p in (%ports%) do (
    echo.
    echo 正在查找端口 %%p ...
    set "found=false"
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr "LISTENING" ^| findstr ":%%p"') do (
        echo 端口 %%p 的 PID: %%a
        taskkill /F /PID %%a
        set "found=true"
    )
    if "!found!"=="false" (
        echo 未找到占用端口 %%p 的进程
    )
)

echo.
echo 操作完成！
pause
