package com.example.task;

import com.example.common.LogAOP;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class DatabaseBackupTask {
   /* 定义一个名为DatabaseBackupTask的类，该类用于执行数据库备份任务
   声明一个私有成员变量resourceLoader，用于加载绝对地址
  */
    private final ResourceLoader resourceLoader;
    public DatabaseBackupTask(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
    }
    // 使用@Scheduled注解定义一个定时任务，每周六2点备份
    @LogAOP(title = "备份", content = "定时全量备份数据库")
    @Scheduled(cron = "0 0 2 ? * 6")
    public void FullBackupDatabase() {

        //定义系统备份的自我操作员id,同时也代表为全量备份文件。
        int operatorId = -1;
        // 使用mysqldump工具备份数据库
        try{
            String osName = System.getProperty("os.name");
            String command;
            ProcessBuilder processBuilder;
            if (osName.contains("Windows")) {//incremental_backup.bat
                Resource resource = resourceLoader.getResource("classpath:BackUp_shell/full_backup_db.bat");
                command = resource.getFile().getAbsolutePath();//获取脚本文件的绝对地址
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command,String.valueOf(operatorId));//执行脚本
            }
            else
            {
                Resource resource = resourceLoader.getResource("classpath:BackUp_shell/full_backup_db.sh");
                command = resource.getFile().getAbsolutePath();//获取脚本文件的绝对地址
                processBuilder = new ProcessBuilder("bash", command);
                processBuilder.command().add(0, "chmod");
                processBuilder.command().add(1, "+x");

            }
            Process process = processBuilder.start();
            //读取脚本输出并打印到控制台
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int Bitcode= process.waitFor();
            if(Bitcode==0) {
                System.out.println("数据库完全备份成功");
            }
            else if(Bitcode==1)
            {
                System.out.println("完全备份目录结构未创建成功，错误代码为："+Bitcode);
            }
            else if(Bitcode==2)
            {
                System.out.println("完全备份目录检查结果失败，错误代码为："+Bitcode);
            }
            else if(Bitcode==3)
            {
                System.out.println("检查状态记录结果失败，错误代码为："+Bitcode);
            }
            else if(Bitcode==4){
                System.out.println("完全备份文件检验失败，错误代码为："+Bitcode);
            }
            else {
                System.out.println("数据库完全备份失败,错误代码为："+Bitcode);
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
    // 使用@Scheduled注解定义一个定时任务，每天3点备份
    @LogAOP(title = "备份", content = "定时差异备份数据库")
    @Scheduled(cron = "0 0 3 * * ?")
    public void IncrementalBackupDatabase() {

        //定义系统备份的自我操作员id,同时也代表为差异备份文件。
        int operatorId = -2;
        // 使用mysqldump工具备份数据库
        try{
            String osName = System.getProperty("os.name");
            String command;
            ProcessBuilder processBuilder;
            if (osName.contains("Windows")) {//incremental_backup.bat
                Resource resource = resourceLoader.getResource("classpath:BackUp_shell/incremental_backup.bat");
                command = resource.getFile().getAbsolutePath();//获取脚本文件的绝对地址
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command,String.valueOf(operatorId));//执行脚本
            }
            else
            {
                Resource resource = resourceLoader.getResource("classpath:BackUp_shell/full_backup_db.sh");
                command = resource.getFile().getAbsolutePath();//获取脚本文件的绝对地址
                processBuilder = new ProcessBuilder("bash", command);
                processBuilder.command().add(0, "chmod");
                processBuilder.command().add(1, "+x");

            }
            Process process = processBuilder.start();
            //读取脚本输出并打印到控制台
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int Bitcode= process.waitFor();
            if(Bitcode==0) {
                System.out.println("数据库差异备份成功");
            }
            else if(Bitcode==5)
            {
                System.out.println("无法创建差异备份目录，错误代码为："+Bitcode);
            }
            else if(Bitcode==6)
            {
                System.out.println("未找到有效全量备份，错误代码为："+Bitcode);
            }
            else if(Bitcode==7)
            {
                System.out.println("全量备份状态文件不存在，错误代码为："+Bitcode);
            }
            else if(Bitcode==8){
                System.out.println("无效的binlog文件信息，错误代码为："+Bitcode);
            }
            else if(Bitcode==9){
                System.out.println("处理失败 [!CURRENT_FILE!]，错误代码为："+Bitcode);
            }
            else if(Bitcode==10){
                System.out.println("没有可用的binlog文件，错误代码为："+Bitcode);
            }
            else if(Bitcode==11){
                System.out.println("备份文件为空，错误代码为："+Bitcode);
            }
            else {
                System.out.println("数据库差异备份失败,错误代码为："+Bitcode);
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
