package com.shortvideo.service;

import com.shortvideo.entity.Comment;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.entity.User;
import java.util.List;

public interface AdminService {

    // 视频管理
    List<SvVideo> getAllVideos();
    void deleteVideo(Long id);

    // 评论管理
    List<Comment> getAllComments();
    void deleteComment(Long id);

    // 用户管理
    List<User> getAllUsers();
    void deleteUser(Long id);

    // 按视频ID查询评论
    List<Comment> getCommentsByVideoId(Long videoId);
}