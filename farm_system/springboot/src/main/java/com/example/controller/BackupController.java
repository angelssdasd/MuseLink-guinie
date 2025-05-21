package com.example.controller;

import com.example.entity.Backup;
import com.example.common.Result;
import com.example.service.BackupService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
}
