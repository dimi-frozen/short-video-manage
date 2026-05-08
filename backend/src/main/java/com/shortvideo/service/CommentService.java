package com.shortvideo.service;

import com.shortvideo.entity.Comment;
import java.util.List;

public interface CommentService {
    
    /**
     * 添加一级评论
     */
    Comment addComment(Long videoId, Long userId, String userName, String content);
    
    /**
     * 回复评论（二级评论）
     */
    Comment replyComment(Long videoId, Long parentId, Long userId, String userName, String content);
    
    /**
     * 根据视频ID获取评论列表（包含二级评论）
     */
    List<Comment> getCommentsByVideoId(Long videoId);
    
    /**
     * 根据父评论ID获取子评论列表
     */
    List<Comment> getChildComments(Long parentId);
    
    /**
     * 根据视频ID获取评论数量
     */
    Long getCommentCountByVideoId(Long videoId);
    
    /**
     * 删除评论（软删除）
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @param videoOwnerId 视频所有者ID
     */
    void deleteComment(Long commentId, Long userId, Long videoOwnerId);
}