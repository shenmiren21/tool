@echo off
:: 设置代码页为UTF-8以支持中文显示
chcp 65001 >nul 2>&1
setlocal enabledelayedexpansion

:: ============================================================================
:: Universal Environment Variable Setter Script
:: ============================================================================
:: Description: Sets system environment variables based on user input
::              (variable name and path value)
:: ============================================================================

:: ----------------------------------------------------------------------------
:: Phase 0: Administrator Check & Self-Elevation
:: ----------------------------------------------------------------------------
:check_admin
echo 正在检查管理员权限...
net session >nul 2>&1
if %errorLevel% == 0 (
    echo 成功：正在以管理员权限运行。
    echo.
    goto :main_logic
)

echo.
echo =================================================================================
echo  警告：需要管理员权限。
echo  正在尝试以提升的权限重新启动脚本。
echo  将出现用户账户控制(UAC)提示，请点击"是"。
echo =================================================================================
echo.

:: 以管理员身份重新启动脚本
powershell -Command "Start-Process -FilePath '%~f0' -Verb RunAs" >nul 2>&1

:: 检查powershell命令本身是否失败（例如，系统上没有PowerShell）
if %errorLevel% neq 0 (
    echo.
    echo 错误：无法触发自提升。
    echo 这可能发生在没有PowerShell的旧版本Windows上。
    echo 请右键点击脚本并手动选择"以管理员身份运行"。
    echo.
    pause
)

:: 退出当前的非提升实例。用户在新窗口中继续。
exit /b


:main_logic
:: ----------------------------------------------------------------------------
:: Phase 1: Get Environment Variable Information
:: ----------------------------------------------------------------------------
echo --- 阶段 1: 环境变量配置 ---
echo.

:: 获取环境变量名称
set "VAR_NAME="
set /p "VAR_NAME=请输入环境变量名称: "

:: 验证变量名称不为空
if not defined VAR_NAME (
    echo.
    echo 错误：环境变量名称不能为空。
    echo 操作已取消。
    pause
    exit /b
)

:: 验证变量名称格式（只允许字母、数字和下划线）
echo %VAR_NAME%| findstr /r "^[A-Za-z_][A-Za-z0-9_]*$" >nul
if %errorLevel% neq 0 (
    echo.
    echo 错误：环境变量名称格式无效。
    echo 变量名只能包含字母、数字和下划线，且不能以数字开头。
    echo 操作已取消。
    pause
    exit /b
)

echo.
echo 环境变量名称: %VAR_NAME%
echo.

:: 获取环境变量值（路径）
set "VAR_VALUE="
set /p "VAR_VALUE=请输入环境变量值（路径）: "

:: 验证路径不为空
if not defined VAR_VALUE (
    echo.
    echo 错误：环境变量值不能为空。
    echo 操作已取消。
    pause
    exit /b
)

:: 路径验证和规范化
set "NORMALIZED_PATH=%VAR_VALUE%"

:: 移除路径末尾的反斜杠（如果存在）
if "%NORMALIZED_PATH:~-1%"=="\" set "NORMALIZED_PATH=%NORMALIZED_PATH:~0,-1%"

:: 检查路径是否存在
if not exist "%NORMALIZED_PATH%\" (
    echo.
    echo 警告：指定的路径不存在: %NORMALIZED_PATH%
    set /p "CONTINUE=是否仍要设置此环境变量？(Y/N): "
    if /i not "!CONTINUE!"=="Y" (
        echo 操作已取消。
        pause
        exit /b
    )
)

echo.
echo 环境变量值: %NORMALIZED_PATH%
echo.

:: ----------------------------------------------------------------------------
:: Phase 2: Check Existing Variable
:: ----------------------------------------------------------------------------
echo --- 阶段 2: 检查现有环境变量 ---
echo.

:: 检查系统环境变量是否已存在
reg query "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v %VAR_NAME% >nul 2>&1
if %errorLevel% == 0 (
    echo 环境变量 '%VAR_NAME%' 已存在。
    
    :: 获取当前值
    for /f "tokens=1,2,*" %%a in ('reg query "HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v %VAR_NAME% ^| find "%VAR_NAME%"') do (
        echo 当前值: %%c
    )
    
    echo.
    set /p "OVERWRITE=是否要覆盖现有值？(Y/N): "
    if /i not "!OVERWRITE!"=="Y" (
        echo 操作已取消。
        pause
        exit /b
    )
) else (
    echo 环境变量 '%VAR_NAME%' 不存在，将创建新的环境变量。
)
echo.

:: ----------------------------------------------------------------------------
:: Phase 3: Set Environment Variable
:: ----------------------------------------------------------------------------
echo --- 阶段 3: 设置环境变量 ---
echo.

echo 正在设置系统环境变量 '%VAR_NAME%' 为 '%NORMALIZED_PATH%'...

:: 使用setx /M设置系统级环境变量。需要管理员权限。
setx %VAR_NAME% "%NORMALIZED_PATH%" /M

if !errorlevel! == 0 (
    echo 环境变量 '%VAR_NAME%' 设置成功。
    echo.
    echo 注意：您可能需要重启应用程序或打开新的命令提示符
    echo 以使新的环境变量生效。
) else (
    echo 错误：无法设置环境变量 '%VAR_NAME%'。
    echo 请检查变量名称和值是否有效。
)
echo.

:: ----------------------------------------------------------------------------
:: Phase 4: Summary
:: ----------------------------------------------------------------------------
echo --- 阶段 4: 操作摘要 ---
echo.
echo ===================================================
echo  环境变量设置完成
echo ===================================================
echo.
echo  变量名称: %VAR_NAME%
echo  变量值: %NORMALIZED_PATH%
echo  作用域: 系统级（所有用户）
echo.
echo  要验证设置，请在新的命令提示符中运行:
echo  echo %%%VAR_NAME%%%
echo.
echo ===================================================
echo.

pause
endlocal
