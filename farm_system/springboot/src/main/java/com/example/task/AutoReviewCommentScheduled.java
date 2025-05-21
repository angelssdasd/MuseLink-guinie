package com.example.task;


import cn.hutool.core.date.DateTime;
import com.example.entity.Comment;
import com.example.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutoReviewCommentScheduled {
    @Autowired
    private CommentService  commentService;

    /*
    10分钟自动评论审核
     */
    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void AutoReviewComment(){
        DateTime now = DateTime.now();
        System.out.println("当前时间：" + now);

        LocalDateTime expirationTime = now.toLocalDateTime().minusMinutes(12);
        List<Comment> comments = commentService.selectByPublishTime(expirationTime);

        for (Comment comment : comments) {
            if (sensitiveWordFilter.containsSensitiveWords(comment.getContent())) {
                commentService.deleteById(comment.getCommentId());
                System.out.println("删除了敏感词：" + comment.getContent());
            }
            else {
                comment.setReviewStatus("通过");
                commentService.update(comment);
                System.out.println("审核通过：" + comment.getContent());
            }
        }
    }

}
