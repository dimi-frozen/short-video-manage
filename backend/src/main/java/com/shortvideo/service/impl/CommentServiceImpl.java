package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shortvideo.entity.Comment;
import com.shortvideo.mapper.CommentMapper;
import com.shortvideo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentMapper commentMapper;
    
    @Override
    @Transactional
    public Comment addComment(Long videoId, Long userId, String content) {
        if (videoId == null || userId == null) {
            throw new IllegalArgumentException("视频ID和用户ID不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过500字符");
        }
        
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setCreateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        return comment;
    }
    
    @Override
    public List<Comment> getCommentsByVideoId(Long videoId) {
        if (videoId == null) {
            throw new IllegalArgumentException("视频ID不能为空");
        }
        
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId)
                   .orderByDesc("create_time");
        
        return commentMapper.selectList(queryWrapper);
    }
    
    @Override
    public Long getCommentCountByVideoId(Long videoId) {
        if (videoId == null) {
            return 0L;
        }
        
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId);
        
        return commentMapper.selectCount(queryWrapper);
    }
}