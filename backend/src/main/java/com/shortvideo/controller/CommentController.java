package com.shortvideo.controller;

import com.shortvideo.entity.Comment;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.entity.User;
import com.shortvideo.mapper.SvVideoMapper;
import com.shortvideo.mapper.UserMapper;
import com.shortvideo.service.CommentService;
import com.shortvideo.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserMapper userMapper;
    private final SvVideoMapper svVideoMapper;

    /**
     * 发布一级评论
     * POST /api/comment/add
     */
    @PostMapping("/add")
    public Result<Comment> addComment(
            @RequestParam Long videoId,
            @RequestParam String content) {
        // 从认证信息中获取当前用户ID
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId;
        if (principal instanceof Long) {
            userId = (Long) principal;
        } else {
            return Result.error("用户未登录");
        }
        
        // 获取用户名
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getName() : "匿名用户";

        Comment comment = commentService.addComment(videoId, userId, userName, content);
        return Result.success(comment);
    }
    
    /**
     * 回复评论（二级评论）
     * POST /api/comment/reply
     */
    @PostMapping("/reply")
    public Result<Comment> replyComment(
            @RequestParam Long videoId,
            @RequestParam Long parentId,
            @RequestParam String content) {
        // 从认证信息中获取当前用户ID
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId;
        if (principal instanceof Long) {
            userId = (Long) principal;
        } else {
            return Result.error("用户未登录");
        }
        
        // 获取用户名
        User user = userMapper.selectById(userId);
        String userName = user != null ? user.getName() : "匿名用户";

        Comment comment = commentService.replyComment(videoId, parentId, userId, userName, content);
        return Result.success(comment);
    }

    /**
     * 查询评论列表（一级评论）
     * GET /api/comment/list?videoId=xxx
     */
    @GetMapping("/list")
    public Result<List<Comment>> getCommentList(
            @RequestParam Long videoId) {
        List<Comment> comments = commentService.getCommentsByVideoId(videoId);
        return Result.success(comments);
    }
    
    /**
     * 查询子评论列表（二级评论）
     * GET /api/comment/children?parentId=xxx
     */
    @GetMapping("/children")
    public Result<List<Comment>> getChildComments(
            @RequestParam Long parentId) {
        List<Comment> comments = commentService.getChildComments(parentId);
        return Result.success(comments);
    }
    
    /**
     * 删除评论
     * DELETE /api/comment/{commentId}?videoId=xxx
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long videoId) {
        // 从认证信息中获取当前用户ID
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId;
        if (principal instanceof Long) {
            userId = (Long) principal;
        } else {
            return Result.error("用户未登录");
        }
        
        // 获取视频所有者ID（用于权限检查）
        SvVideo video = svVideoMapper.selectById(videoId);
        Long videoOwnerId = video != null ? video.getUserId() : null;
        
        commentService.deleteComment(commentId, userId, videoOwnerId);
        return Result.success();
    }
}