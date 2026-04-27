package com.shortvideo.controller;

import com.shortvideo.entity.Comment;
import com.shortvideo.service.CommentService;
import com.shortvideo.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发布评论
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

        Comment comment = commentService.addComment(videoId, userId, content);
        return Result.success(comment);
    }

    /**
     * 查询评论列表
     * GET /api/comment/list?videoId=xxx
     */
    @GetMapping("/list")
    public Result<List<Comment>> getCommentList(
            @RequestParam Long videoId) {
        List<Comment> comments = commentService.getCommentsByVideoId(videoId);
        return Result.success(comments);
    }
}