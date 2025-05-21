package com.example.controller;

import com.example.common.Result;
import com.example.entity.review;
import com.example.service.ReviewService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController // 注意这里首字母大写
@RequestMapping("/review") // 注意这里首字母大写
public class ReviewController {

    @Resource
    private ReviewService reviewService;


    @GetMapping("/selectPage")
    public Result selectPage(
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             String reviewerId) {
        int h=3;
        PageInfo<review> page = reviewService.list(pageNum, pageSize, reviewerId);
        return Result.success(page);
    }
    @GetMapping("/{id}")
    public review getById(@PathVariable Integer id) {
        return reviewService.getById(id);
    }

    @PostMapping("/add")
    public boolean add(@RequestBody review review) { // 注意这里首字母大写
        return reviewService.add(review);
    }

    @PutMapping("/update")
    public boolean update(@RequestBody review review) { // 注意这里首字母大写
        return reviewService.update(review);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        return reviewService.delete(id);
    }
}