@echo off
setlocal enabledelayedexpansion

REM 操作员ID
set operatorId=%1

REM ---------- 数据库配置 ----------
set DB_NAME=manage
set BACKUP_ROOT=C:\backup
set DIFF_BACKUP_ROOT=C:\diff_backup
set BINLOG_DIR=D:\MYSQL\MySQL Server 8.0\Data\log
set MYSQL_HOME=D:\MYSQL\MySQL Server 8.0
set MY_INI=D:\IDEA_project\zuoye\farm_system\springboot\src\main\resources\BackUp_shell\my.ini

REM ---------- 日期参数 ----------
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set DATE=!datetime:~0,14!
set DIFF_BACKUP_SUBDIR=!DIFF_BACKUP_ROOT!\!DATE!
set DIFF_BACKUP_FILE=!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff-!DATE!.sql

REM sql语句代码
set path=C:\\diff_backup\\!DATE!\\!DB_NAME!-diff-!DATE!.sql

REM ---------- 目录创建 ----------
if not exist "!DIFF_BACKUP_ROOT!" mkdir "!DIFF_BACKUP_ROOT!"
if not exist "!DIFF_BACKUP_SUBDIR!" (
    mkdir "!DIFF_BACKUP_SUBDIR!"
    if !errorlevel! neq 0 (
        echo [%DATE% %TIME%] ERROR: 无法创建差异备份目录 >> "!DIFF_BACKUP_ROOT!\diff_error.log"
        exit /b 5
    )
)

REM ---------- 日志初始化 ----------
echo [%DATE% %TIME%] 开始差异备份操作 > "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
echo [DEBUG] 备份文件路径: !DIFF_BACKUP_FILE! >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"

REM ---------- 查找最新全量备份 ----------
set LATEST_FULL=
for /f "delims=" %%D in ('dir /ad /b /o-n "!BACKUP_ROOT!\*"') do (
    set "folder=%%D"
    echo %%D
    if exist "!BACKUP_ROOT!\\!folder!\\!DB_NAME!-master-status-*.txt" (
        set LATEST_FULL=!folder!
        goto :found_full_backup
    )
)

:found_full_backup
if not defined LATEST_FULL (
    echo [%DATE% %TIME%] ERROR: 未找到有效全量备份 >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
    exit /b 6
)

REM ---------- 获取binlog位置 ----------
set MASTER_STATUS_FILE=
for %%F in ("!BACKUP_ROOT!\!LATEST_FULL!\!DB_NAME!-master-status-*.txt") do set MASTER_STATUS_FILE=%%F

if not exist "!MASTER_STATUS_FILE!" (
    echo [%DATE% %TIME%] ERROR: 全量备份状态文件不存在 >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
    exit /b 7
)

REM ---------- 解析binlog信息 ----------
set BINLOG_FILE=
set BINLOG_POS=
for /f "tokens=1-2" %%A in ('type "!MASTER_STATUS_FILE!"') do (
    set BINLOG_FILE=%%A
    set BINLOG_POS=%%B
)

if "!BINLOG_POS!"=="0" set BINLOG_POS=4
if not defined BINLOG_FILE (
    echo [%DATE% %TIME%] ERROR: 无效的binlog文件信息 >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
    exit /b 8
)

REM ---------- 处理binlog文件 ----------
echo [%DATE% %TIME%] 正在处理binlog文件... >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
dir /b /on "!BINLOG_DIR!\!DB_NAME!-bin.*" > "!DIFF_BACKUP_SUBDIR!\temp.txt"

set FIRST_FILE=1
set FILE_COUNT=0
for /f "delims=" %%F in ('type "!DIFF_BACKUP_SUBDIR!\temp.txt"') do (
    set CURRENT_FILE=%%F

    if "!CURRENT_FILE!"=="!DB_NAME!-bin.index" (
        echo [%DATE% %TIME%] 跳过索引文件 >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
    ) else (
        if "!CURRENT_FILE!" geq "!BINLOG_FILE!" (
            set /a FILE_COUNT+=1
            echo [%DATE% %TIME%] 正在处理: !CURRENT_FILE! >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"

            if !FIRST_FILE! equ 1 (
                "%MYSQL_HOME%\bin\mysqlbinlog.exe" ^
                    --defaults-extra-file="!MY_INI!" ^
                    --force-if-open ^
                    --start-position=!BINLOG_POS! ^
                    "!BINLOG_DIR!\!CURRENT_FILE!" >> "!DIFF_BACKUP_FILE!"
                set FIRST_FILE=0
            ) else (
                "%MYSQL_HOME%\bin\mysqlbinlog.exe" ^
                    --defaults-extra-file="!MY_INI!" ^
                    --force-if-open ^
                    --start-position=4 ^
                    "!BINLOG_DIR!\!CURRENT_FILE!" >> "!DIFF_BACKUP_FILE!"
            )

            if !errorlevel! neq 0 (
                echo [%DATE% %TIME%] ERROR: 处理失败 [!CURRENT_FILE!] >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
                exit /b 9
            )
        )
    )
)

REM ---------- 清理和验证 ----------
del "!DIFF_BACKUP_SUBDIR!\temp.txt"

if !FILE_COUNT! equ 0 (
    echo [%DATE% %TIME%] ERROR: 没有可用的binlog文件 >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
    exit /b 10
)

for %%F in ("!DIFF_BACKUP_FILE!") do (
    if %%~zF leq 0 (
        echo [%DATE% %TIME%] ERROR: 备份文件为空 >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
        del "!DIFF_BACKUP_FILE!"
        exit /b 11
    )
)

echo [%DATE% %TIME%] 差异备份成功! 处理文件数: !FILE_COUNT! >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"
echo 最新全量备份: !LATEST_FULL! >> "!DIFF_BACKUP_SUBDIR!\!DB_NAME!-diff.log"

if !errorlevel! equ 0 (
    "%MYSQL_HOME%\bin\mysql.exe" ^
        --defaults-extra-file="%MY_INI%" ^
        --database=manage ^
        --execute="use manage; INSERT INTO backup (backup_time, file_path, operator_id) VALUES ('%DATE%', '%path%',%operatorId%);"
)
endlocal