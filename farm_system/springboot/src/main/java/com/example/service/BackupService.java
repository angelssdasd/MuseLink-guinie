package com.example.service;

import com.example.common.Result;
import com.example.entity.Backup;
import com.example.mapper.BackupMapper;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BackupService {
    @Resource
    private BackupMapper backupMapper;
    private final ResourceLoader resourceLoader;
    public BackupService(ResourceLoader resourceLoader) {
        this.resourceLoader= resourceLoader;
    }


// 根据条件查询Backup列表
    public List<Backup>selectByCondition(Backup backup) {
        // 调用backupMapper的selectByCondition方法，传入backup的operatorId参数
        return backupMapper.selectByCondition(backup.getOperatorId());
    }

    public String getFilePath(Integer backupId) {
        return backupMapper.getFilePath(backupId);
    }

    //根据文件路径删除记录
    public void deleteByFilePath(String filePath) {
        try{
            if(this.isFullBackupFile(filePath))
            {
                LocalDateTime FirstTime=backupMapper.getBackupTime(filePath);
                List<Backup>SecondFile=backupMapper.selectByTime(FirstTime);
                if(SecondFile.isEmpty())
                {
                    deleteParentDirectory(filePath);
                }
                else{
                    List<Backup> diffBackups = new ArrayList<>();
                    for (Backup backup : SecondFile) {
                        if (isFullBackupFile(backup.getFilePath())) {
                            diffBackups.add(backup);
                        }
                    }
                    if(diffBackups.isEmpty()){
                        for(Backup backup : SecondFile){
                            deleteParentDirectory(backup.getFilePath());
                        }
                    }
                    else{
                        diffBackups.sort(Comparator.comparing(Backup::getBackupTime));
                        LocalDateTime SecondBackupTime =backupMapper.getBackupTime(diffBackups.getFirst().getFilePath());
                        List<Backup> diffFileList = backupMapper.selectByInterval(FirstTime,  SecondBackupTime);
                        for(Backup backup : diffFileList){
                            deleteParentDirectory(backup.getFilePath());
                        }
                    }
                }
                deleteParentDirectory(filePath);
            }else{
                // 调用backupMapper的deleteByFilePath方法，传入filePath参数
                deleteParentDirectory(filePath);
            }
        }catch (Exception e)
            {
                System.err.println("删除文件失败: " + filePath);
                e.printStackTrace();
            }
    }
    public Result JudgeFilePath(ArrayList<Integer> backupIdList)
    {
        if(backupIdList.size()==1){
            String FilePath=this.getFilePath(backupIdList.getFirst());
            ArrayList<String> filePathList=new ArrayList<>();
            Collections.addAll(filePathList, FilePath.split("-"));
            if(filePathList.get(1).equals("full"))
            {
               return Result.success();
            }
            else{
                return Result.error("不是全量备份");
            }
        }
        else{
            String FilePath1=this.getFilePath(backupIdList.getFirst());
            String FilePath2=this.getFilePath(backupIdList.getLast());
            ArrayList<String> filePathList1=new ArrayList<>();
            ArrayList<String> filePathList2=new ArrayList<>();
            Collections.addAll(filePathList1, FilePath1.split("-"));
            Collections.addAll(filePathList2, FilePath2.split("-"));
            int Date1=Integer.parseInt(filePathList1.get(2).substring(4,14));
            int Date2=Integer.parseInt(filePathList2.get(2).substring(4,14));
            if(filePathList1.get(1).equals(filePathList2.get(1))){
                return Result.error("请选择一份完全备份和差异备份文件");
            }
            else{
                if(Date1>Date2&&filePathList1.get(1).equals("full")){
                    return Result.error("完全备份和差异备份文件时间不对应");
                }
                else if(Date1<Date2&&filePathList2.get(1).equals("full")) {
                    return Result.error("完全备份和差异备份文件时间不对应");
                }
                else if(Math.abs(Date1-Date2)>6000000){
                    return Result.error("完全备份和差异备份文件时间相差过大");
                }
                else{
                    return Result.success();
                }
            }
        }
    }
    /**
     * 恢复备份
     */
    public Result restore(ArrayList<Integer> backupIdList){
        String command;
        ProcessBuilder processBuilder;
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:BackUp_shell/restore.bat");
        try{
            command = resource.getFile().getAbsolutePath();
        }catch (Exception e){
            return Result.error("找不到恢复命令");
        }

        if(backupIdList.size()==1) {
            String FilePath = this.getFilePath(backupIdList.getFirst());
            processBuilder = new ProcessBuilder(command, FilePath);
            processBuilder.redirectErrorStream(true);
        }
        else{
            String FilePath1 = this.getFilePath(backupIdList.getFirst());
            String FilePath2 = this.getFilePath(backupIdList.getLast());
            ArrayList<String> filePathList=new ArrayList<>();
            Collections.addAll(filePathList, FilePath1.split("-"));
            if(filePathList.get(1).equals("full")){
                processBuilder = new ProcessBuilder(command, FilePath1, FilePath2);
            }
            else{
                processBuilder = new ProcessBuilder(command, FilePath2, FilePath1);
            }
            System.out.println("执行命令：" + Arrays.toString(new String[]{command, FilePath1, FilePath2}));

        }
        try {
            Process process = processBuilder.start();
            processBuilder.redirectErrorStream(true);
            // 读取脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            processBuilder.redirectErrorStream(true);
            System.out.println("执行命令：" + processBuilder.command());
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待脚本执行完成
            int exitCode = process.waitFor();
            // 读取脚本的错误输出
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("错误输出: " + errorLine);
            }

            if(exitCode==0){
                return Result.success();
            }
            else{
                return Result.error("命令运行失败，错误码为"+exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Result.error("服务运行失败");
        }
    }
        /*
        * 获取过期的全量文件列表
         */
    public List<String> GetExpirationFile(LocalDateTime expirationTime) {
        List<String> expiredFiles = backupMapper.getExpiredFiles(expirationTime);
        List<String> fullBackupFiles = new ArrayList<>();
        for (String sqlFilePath : expiredFiles) {
            if (isFullBackupFile(sqlFilePath)) {
                fullBackupFiles.add(sqlFilePath);
            }
        }
        return fullBackupFiles;
    }

    /*
    * 根据全量文件时间获取对应的差异文件列表
     */
    public List<String> GetDiffFile(LocalDateTime fullBackupTime) {
        LocalDateTime DeadlineTime = fullBackupTime.plusDays(7);
        List<String> Files = backupMapper.getExpiredFiles(DeadlineTime);
        List<String>DiffFFiles= new ArrayList<>();
        for (String sqlFilePath : Files) {
            if (!isFullBackupFile(sqlFilePath)) {
                DiffFFiles.add(sqlFilePath);
            }
        }
       return DiffFFiles;
    }

    /**
     * 判断是否是 manage-full- 开头的 SQL 文件
     *
     * @param sqlFilePath .sql 文件路径
     * @return 如果是 full 类型的备份文件，返回 true；否则返回 false
     */
    private boolean isFullBackupFile(String sqlFilePath) {
        File sqlFile = new File(sqlFilePath);
        String fileName = sqlFile.getName(); // 获取文件名，例如 "manage-full-20250516010135.sql"

        // 判断文件名是否以 "manage-full-" 开头且包含 ".sql"
        return fileName.startsWith("manage-full-") && fileName.endsWith(".sql");
    }
    /*
    * 根据全量文件路径获取对应的差异文件列表
     */
    public List<String> GetDiffFile(String fullBackupFilePath) {
        List<String> diffFiles;
        LocalDateTime fullBackupTime =backupMapper.getBackupTime(fullBackupFilePath);
        diffFiles=GetDiffFile(fullBackupTime);
        return diffFiles;
    }

    /*
    * 开始备份
     */

    @Transactional
public Result start(String type, String userId) {
    String command;
    ProcessBuilder processBuilder;
    try {
        if (type.equals("full")) {
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:BackUp_shell/full_backup_db.bat");
            command = resource.getFile().getAbsolutePath(); // 获取脚本文件的绝对地址
        } else {
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:BackUp_shell/incremental_backup.bat");
            command = resource.getFile().getAbsolutePath(); // 获取脚本文件的绝对地址
        }
        processBuilder = new ProcessBuilder("cmd.exe", "/c", command, String.valueOf(userId));
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return Result.success();
        } else {
            return Result.error("命令运行失败，错误码为" + exitCode);
        }

    } catch (IOException | InterruptedException e) {
        return Result.error("执行备份脚本失败：" + e.getMessage());
        }
    }

    /**
     * 删除 .sql 文件、.txt 文件及对应的 diff 目录
     */
    private void deleteParentDirectory(String sqlFilePath) throws IOException {
        File sqlFile = new File(sqlFilePath);
        if (!sqlFile.exists()) {
            System.out.println("SQL文件不存在: " + sqlFilePath);
            return;
        }
        // 获取上级目录（例如 C:\backup\20250516010135\）
        File parentDir = sqlFile.getParentFile();
        // 删除整个目录（包含 .sql、.txt 等所有文件）
        if (parentDir.exists() && parentDir.isDirectory()) {
            deleteDirectory(parentDir);
            System.out.println("删除上级目录: " + parentDir.getAbsolutePath() + " -> 成功");
        }
        backupMapper.deleteByFilePath(sqlFilePath);
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

    public boolean JudgeExist(Integer backupId) {
        return  backupMapper.getFilePath(backupId)!=null;
    }
}
