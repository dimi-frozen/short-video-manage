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
    public Comment addComment(Long videoId, Long userId, String userName, String content) {
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
        comment.setParentId(null);  // 一级评论
        comment.setUserId(userId);
        comment.setUserName(userName);  // 保存用户名
        comment.setContent(content.trim());
        comment.setCreateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        return comment;
    }
    
    @Override
    @Transactional
    public Comment replyComment(Long videoId, Long parentId, Long userId, String userName, String content) {
        if (videoId == null || parentId == null || userId == null) {
            throw new IllegalArgumentException("视频ID、父评讻ID和用户ID不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过500字符");
        }
            
        // 验证父评论是否存在
        Comment parentComment = commentMapper.selectById(parentId);
        if (parentComment == null) {
            throw new IllegalArgumentException("父评论不存在");
        }
            
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setParentId(parentId);  // 二级评论
        comment.setUserId(userId);
        comment.setUserName(userName);  // 保存用户名
        comment.setReplyToUserName(parentComment.getUserName());  // 保存回复的目标用户名
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
        
        // 只查询一级评论（parent_id 为 NULL）且未删除的评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId)
                   .isNull("parent_id")
                   .eq("is_deleted", false)  // 过滤已删除的评论
                   .orderByDesc("create_time");
        
        return commentMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Comment> getChildComments(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("父评论ID不能为空");
        }
        
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId)
                   .eq("is_deleted", false)  // 过滤已删除的子评论
                   .orderByAsc("create_time");
        
        return commentMapper.selectList(queryWrapper);
    }
    
    @Override
    public Long getCommentCountByVideoId(Long videoId) {
        if (videoId == null) {
            return 0L;
        }
        
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId)
                   .eq("is_deleted", false);  // 只统计未删除的评论
        
        return commentMapper.selectCount(queryWrapper);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId, Long videoOwnerId) {
        if (commentId == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        // 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        // 如果已经删除，直接返回
        if (comment.getIsDeleted()) {
            return;
        }
        
        // 权限检查：只有评论发布者或视频所有者可以删除
        boolean isCommentOwner = comment.getUserId().equals(userId);
        boolean isVideoOwner = videoOwnerId != null && videoOwnerId.equals(userId);
        
        if (!isCommentOwner && !isVideoOwner) {
            throw new IllegalArgumentException("没有权限删除此评论");
        }
        
        // 软删除：只标记is_deleted为true，不物理删除
        comment.setIsDeleted(true);
        // 如果有子评论，清空内容并显示"该评论已删除"
        if (hasChildComments(commentId)) {
            comment.setContent("该评论已删除");
        }
        commentMapper.updateById(comment);
    }
    
    /**
     * 检查评论是否有子评论
     */
    private boolean hasChildComments(Long parentId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId)
                   .eq("is_deleted", false);
        Long count = commentMapper.selectCount(queryWrapper);
        return count > 0;
    }
}