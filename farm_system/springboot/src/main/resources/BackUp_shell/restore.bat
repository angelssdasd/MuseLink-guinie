@echo off

setlocal enabledelayedexpansion
chcp 65001 > nul

REM 输入参数 ------------------------------------------------
set FULL_BACKUP_PATH=%1
set DIFF_BACKUP_PATH=%2

REM 恢复配置 ------------------------------------------------
set DB_NAME=manage
set MY_INI=D:\IDEA_project\zuoye\farm_system\springboot\src\main\resources\BackUp_shell\my.ini
set MYSQL_HOME=D:\MYSQL\MySQL Server 8.0



REM 检查必要参数 --------------------------------------------
if "%FULL_BACKUP_PATH%"=="" (
    echo Error: 必须指定全量备份文件路径
    echo “用法: restore.bat [全量备份文件] [差异备份文件]”
    / exitb 1
)

REM 初始化日志 ----------------------------------------------
for /f "tokens=2 delims==" %%a in ('wmic os get localdatetime /value 2^>nul') do set "datetime=%%a"
set LOG_FILE=restore_%datetime:~0,8%.log
echo [%TIME%] 开始恢复操作 > "%LOG_FILE%" && type nul >> "%LOG_FILE%"

REM 步骤1：验证备份文件有效性 -------------------------------
echo [%TIME%] 验证全量备份文件: "%FULL_BACKUP_PATH%" >> "%LOG_FILE%"
if not exist %FULL_BACKUP_PATH% (
    echo [%TIME%] "错误：全量备份文件不存在" >> "%LOG_FILE%"
    exit /b 1
)

if "%DIFF_BACKUP_PATH%" neq "" (
    echo [%TIME%] 验证差异备份文件: "%DIFF_BACKUP_PATH%" >> "%LOG_FILE%"
    if not exist "%DIFF_BACKUP_PATH%" (
        echo [%TIME%] "错误：差异备份文件不存在" >> "%LOG_FILE%"
        exit /b 2
    )
)


REM 步骤2：执行全量恢复 -------------------------------------
echo [%TIME%] 开始全量恢复... >> "%LOG_FILE%"
"%MYSQL_HOME%\bin\mysql.exe" --defaults-extra-file="!MY_INI!" --force  %DB_NAME% <%FULL_BACKUP_PATH%

if !errorlevel! neq 0 (
    echo [%TIME%] "错误：全量恢复失败 (错误码: !errorlevel!)" >> "%LOG_FILE%"
    exit /b 3
)
echo [%TIME%] 全量恢复完成 >> "%LOG_FILE%"

REM 步骤3：执行差异恢复（如果提供）---------------------------
if "%DIFF_BACKUP_PATH%" neq "" (
    echo [%TIME%] 开始差异恢复... >> "%LOG_FILE%"
    "%MYSQL_HOME%\bin\mysql.exe" --defaults-extra-file="!MY_INI!" --force %DB_NAME% <%DIFF_BACKUP_PATH%

    if !errorlevel! neq 0 (
        echo [%TIME%] "错误：差异恢复失败 (错误码: !errorlevel!)" >> "%LOG_FILE%"
        exit /b 4
    )
    echo [%TIME%] 差异恢复完成 >> "%LOG_FILE%"
)

REM 最终检查 ------------------------------------------------
echo [%TIME%] 恢复操作成功完成 >> "%LOG_FILE%"
echo [%TIME%] 详细日志见: %LOG_FILE% >> "%LOG_FILE%"
endlocal