package com.example.service;

import com.example.common.LogAOP;
import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    // 查询一定时间内的未通过的评论
    @Autowired
    private CommentMapper commentMapper;
    public List<Comment> selectByPublishTime(LocalDateTime expirationTime) {
        return commentMapper.selectByPublishTime(expirationTime);
    }

    @LogAOP(title = "改", content = "将评论状态改为通过")
    public void update(Comment comment) {
        commentMapper.update(comment);
    }

    @LogAOP(title = "删", content = "删除违规评论")
    public void deleteById(Integer commentId) {
        commentMapper.deleteById(commentId);
    }
}
