package com.example.controller;

import com.example.common.Result;
import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @GetMapping("/all")
    public Result all() {
        return Result.success(commentMapper.selectAll());
    }

    @GetMapping("/select")
    public Result select(@RequestParam(required = false) Integer userId,
                         @RequestParam(required = false) Integer artifactId) {
        return Result.success(commentMapper.selectByCondition(userId, artifactId));
    }

    @PostMapping("/add")
    public Result add(@RequestBody Comment comment) {
        commentMapper.insert(comment);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Comment comment) {
        commentMapper.update(comment);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        commentMapper.deleteById(id);
        return Result.success();
    }
}
