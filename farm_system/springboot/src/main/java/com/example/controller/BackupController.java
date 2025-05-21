package com.example.controller;

import com.example.common.LogAOP;
import com.example.entity.Backup;
import com.example.common.Result;
import com.example.service.BackupService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @GetMapping("/selectPage")
    public Result selectPage(Backup backup,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Backup> list =backupService.selectByCondition(backup);
        return Result.success(new PageInfo<>(list));
    }

    @LogAOP(title = "恢复", content = "恢复数据库")
    @PostMapping("/restore")
    public Result restore(@RequestBody String backupIds) {
        ArrayList<Integer>backupIdList = new ArrayList<>();
        //去掉双引号
        String newBackupIds = backupIds.replaceAll("\"", "");
        for (String s : newBackupIds.split(",")) {
            backupIdList.add(Integer.parseInt(s));
        }
        Result result = backupService.JudgeFilePath(backupIdList);
        if(Objects.equals(result.getCode(), "500")){
            return result;
        }
        else {
            return backupService.restore(backupIdList);
        }
    }
    /*
    开始备份文件
     */
    @LogAOP(title = "备份", content = "备份文件")
    @PostMapping("/start")
    public Result start(@RequestBody Map<String, Object> params ) {
        String type = (String) params.get("type");
        String userId =String.valueOf(params.get("userId"));
        return backupService.start(type, userId);
    }

    /*
    删除备份文件
     */
    @LogAOP(title = "删", content = "删除备份文件")
    @DeleteMapping ("/delete")
    public Result delete(@RequestParam String backupIds) {
        ArrayList<Integer>backupIdList = new ArrayList<>();
        //去掉双引号
        String newBackupIds = backupIds.replaceAll("\"", "");
        for (String s : newBackupIds.split(",")) {
            backupIdList.add(Integer.parseInt(s));
        }
        for (Integer backupId : backupIdList) {
            if(backupService.JudgeExist(backupId))
            {
                String filePath = backupService.getFilePath(backupId);
                backupService.deleteByFilePath(filePath);
            }
        }

        return Result.success();
    }
}
