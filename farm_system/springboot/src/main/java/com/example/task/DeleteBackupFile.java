package com.example.task;

import cn.hutool.core.date.DateTime;
import com.example.common.LogAOP;
import com.example.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeleteBackupFile {

    @Autowired
    private BackupService backupService;

    @LogAOP(title = "删除", content = "删除过期备份文件")
     @Scheduled(cron = "0 0 0 ? * 1")// 每周一凌晨 0 点执行
    public void deleteBackupFile() {
        DateTime now = DateTime.now();
        System.out.println("当前时间：" + now);

        LocalDateTime expirationTime = now.toLocalDateTime().minusDays(21); // 设置过期时间为21天前
        List<String> expiredFiles = backupService.GetExpirationFile(expirationTime); // 获取过期全量文件列表

        for (String sqlFilePath : expiredFiles) {
            try {//删除对应的差异备份
                try {
                    List<String>  diffFiles = backupService.GetDiffFile(sqlFilePath);
                    for (String diffFilePath : diffFiles) {
                        deleteParentDirectory(diffFilePath);
                    }
                    System.out.println("删除差异文件成功: " + sqlFilePath);
                }
                catch (Exception e) {
                    System.err.println("删除差异文件失败: " + sqlFilePath);
                    e.printStackTrace();
                }
                deleteParentDirectory(sqlFilePath);
            } catch (Exception e) {
                System.err.println("删除全量文件失败: " + sqlFilePath);
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除 .sql 文件、.txt 文件及对应的 diff 目录
     */
    private void deleteParentDirectory(String sqlFilePath) throws IOException {
        File sqlFile = new File(sqlFilePath);
        if (!sqlFile.exists()) {
            System.out.println("SQL 文件不存在: " + sqlFilePath);
            return;
        }

        // 获取上级目录（例如 C:\backup\20250516010135\）
        File parentDir = sqlFile.getParentFile();

        // 删除整个目录（包含 .sql、.txt 等所有文件）
        if (parentDir.exists() && parentDir.isDirectory()) {
            deleteDirectory(parentDir);
            System.out.println("删除上级目录: " + parentDir.getAbsolutePath() + " -> 成功");
        }
    // 从数据库中删除记录
        backupService.deleteByFilePath(sqlFilePath);
    }


    /**
     * 递归删除目录及其内容
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file); // 递归删除子目录和文件
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("无法删除目录或文件: " + directory.getAbsolutePath());
        }
    }
}
