@echo off
setlocal enabledelayedexpansion
REM 操作员ID
set operatorId=%1

REM 数据库信息
set DB_NAME=manage

REM 主备份目录和日期
set BACKUP_ROOT=C:\backup
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set DATE=!datetime:~0,14!

REM 创建带日期的子目录
set BACKUP_SUBDIR=!BACKUP_ROOT!\!DATE!
set FULL_BACKUP_FILE=!BACKUP_SUBDIR!\!DB_NAME!-full-!DATE!.sql
set MASTER_STATUS_FILE=!BACKUP_SUBDIR!\!DB_NAME!-master-status-!DATE!.txt

REM sql语句代码
set BACKUP_PATH=C:\\backup\\!DATE!\\!DB_NAME!-full-!DATE!.sql


REM MySQL配置
set MY_INI=D:\IDEA_project\zuoye\farm_system\springboot\src\main\resources\BackUp_shell\my.ini
set MYSQL_HOME=D:\MYSQL\MySQL Server 8.0

REM 创建目录结构
if not exist "!BACKUP_ROOT!" mkdir "!BACKUP_ROOT!"
if not exist "!BACKUP_SUBDIR!" (
    mkdir "!BACKUP_SUBDIR!"
    if !errorlevel! neq 0 (
        echo Error: Failed to create backup directory "!BACKUP_SUBDIR!"
        exit /b 1
    )
)

REM 执行全量备份
echo [%TIME%] Starting full backup...
"%MYSQL_HOME%\bin\mysqldump.exe" ^
    --defaults-extra-file="!MY_INI!" ^
    --single-transaction ^
    --source-data=2 ^
    --flush-logs ^
    --ignore-table=manage.backup ^
    !DB_NAME! > "!FULL_BACKUP_FILE!"

REM 检查备份结果
if !errorlevel! neq 0 (
    echo [%TIME%] Full backup failed >> "!BACKUP_SUBDIR!\backup_error.log"
    exit /b 2
)
echo [%TIME%] Full backup completed: !FULL_BACKUP_FILE!


REM 记录binlog位置
echo [%TIME%] Recording master status...
"%MYSQL_HOME%\bin\mysql.exe" ^
    --defaults-extra-file="!MY_INI!" ^
    --skip-column-names ^
    -e "SHOW MASTER STATUS;" > "!MASTER_STATUS_FILE!"

REM 检查状态记录结果
if !errorlevel! neq 0 (
    echo [%TIME%] Failed to record binlog position >> "!BACKUP_SUBDIR!\backup_error.log"
    exit /b 3
)
echo [%TIME%] Master status recorded: !MASTER_STATUS_FILE!

REM 验证文件生成
set FILE_CHECK_FAILED=0
if not exist "!FULL_BACKUP_FILE!" set FILE_CHECK_FAILED=1
if not exist "!MASTER_STATUS_FILE!" set FILE_CHECK_FAILED=1

if %FILE_CHECK_FAILED% equ 1 (
    echo [%TIME%] Backup files verification failed >> "!BACKUP_SUBDIR!\backup_error.log"
    exit /b 4
)

echo [%TIME%] All backup files generated in: !BACKUP_SUBDIR!


"%MYSQL_HOME%\bin\mysql.exe" ^
    --defaults-extra-file="%MY_INI%" ^
    --database=manage ^
    --execute="use manage; INSERT INTO backup (backup_time, file_path, operator_id) VALUES ('%DATE%', '%BACKUP_PATH%',%operatorId%);"

endlocal