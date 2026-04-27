package com.shortvideo.service;

import com.shortvideo.entity.Comment;
import java.util.List;

public interface CommentService {
    
    /**
     * 添加评论
     */
    Comment addComment(Long videoId, Long userId, String content);
    
    /**
     * 根据视频ID获取评论列表
     */
    List<Comment> getCommentsByVideoId(Long videoId);
    
    /**
     * 根据视频ID获取评论数量
     */
    Long getCommentCountByVideoId(Long videoId);
}