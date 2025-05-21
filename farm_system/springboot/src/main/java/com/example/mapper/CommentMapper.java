package com.example.mapper;

import com.example.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectAll();

    List<Comment> selectByCondition(@Param("userId") Integer userId, @Param("artifactId") Integer artifactId);

    Comment selectById(Integer id);

    void insert(Comment comment);

    void update(Comment comment);

    void deleteById(Integer id);
    // 查询一定时间内的未通过的评论
    List<Comment> selectByPublishTime(LocalDateTime expirationTime);
}
