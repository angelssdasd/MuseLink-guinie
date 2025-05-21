package com.example.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunBatScript {

    public static void main(String[] args) {
        // 定义 bat 脚本的路径
        String batScriptPath = "D:/IDEA_project/zuoye/farm_system/springboot/src/main/resources/BackUp_shell/full_backup_db.bat ";
        String batScriptPath1 = "D:/IDEA_project/zuoye/farm_system/springboot/src/main/resources/BackUp_shell/incremental_backup.bat";
        String batScriptPath2 = "D:/IDEA_project/zuoye/farm_system/springboot/src/main/resources/BackUp_shell/restore.bat";
        // 使用 ProcessBuilder 运行 bat 脚本
        int operatorId = -1;
        ProcessBuilder processBuilder = new ProcessBuilder(batScriptPath1, String.valueOf(operatorId));
        processBuilder.redirectErrorStream(true); // 将错误流合并到输出流

        try {
            Process process = processBuilder.start();

            // 读取脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待脚本执行完成
            int exitCode = process.waitFor();
            System.out.println("Exit Code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
