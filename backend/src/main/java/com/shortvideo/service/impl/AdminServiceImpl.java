package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortvideo.entity.Comment;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.entity.User;
import com.shortvideo.mapper.CommentMapper;
import com.shortvideo.mapper.SvVideoMapper;
import com.shortvideo.mapper.UserMapper;
import com.shortvideo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SvVideoMapper svVideoMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    // 获取所有视频
    @Override
    public List<SvVideo> getAllVideos() {
        return svVideoMapper.selectList(null);
    }

    // 删除视频
    @Override
    public void deleteVideo(Long id) {
        svVideoMapper.deleteById(id);
    }

    // 获取所有评论
    @Override
    public List<Comment> getAllComments() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        return commentMapper.selectList(wrapper);
    }

    // 按视频ID查评论
    @Override
    public List<Comment> getCommentsByVideoId(Long videoId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getVideoId, videoId)
                .orderByDesc(Comment::getCreateTime);
        return commentMapper.selectList(wrapper);
    }

    // 删除评论
    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }

    // 获取所有用户
    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    // 删除用户
    @Override
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 禁止删除管理员账号
        if ("admin".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("不能删除管理员账号");
        }
        userMapper.deleteById(id);
    }
}