package com.shortvideo.controller.admin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortvideo.dto.UserVO;
import com.shortvideo.entity.Comment;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.entity.User;
import com.shortvideo.mapper.UserMapper;
import com.shortvideo.service.AdminService;
import com.shortvideo.utils.JwtUtils;
import com.shortvideo.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    // ==================== 视频管理 ====================
    @GetMapping("/video/all")
    public Result<List<SvVideo>> getAllVideos() {
        return Result.success(adminService.getAllVideos());
    }

    @DeleteMapping("/video/{id}")
    public Result<Void> deleteVideo(@PathVariable Long id) {
        adminService.deleteVideo(id);
        return Result.success();
    }

    // ==================== 评论管理 ====================
    @GetMapping("/comment/all")
    public Result<List<Comment>> getAllComments() {
        return Result.success(adminService.getAllComments());
    }

    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return Result.success();
    }

    // ==================== 用户管理 ====================
    @GetMapping("/user/all")
    public Result<List<User>> getAllUsers() {
        return Result.success(adminService.getAllUsers());
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    /**
     * 管理员登录接口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Result.error("账号或密码不能为空");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email.trim());
        User loginUser = userMapper.selectOne(wrapper);

        if (loginUser == null) {
            return Result.error("账号或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, loginUser.getPasswordHash())) {
            return Result.error("账号或密码错误");
        }

        // 验证角色
        if (!"admin".equals(loginUser.getRole())) {
            return Result.error("非管理员账号");
        }

        log.info("管理员登录成功: {}", email);
        String token = jwtUtils.generateToken(loginUser.getId(), loginUser.getEmail());

        UserVO userVO = new UserVO();
        userVO.setId(loginUser.getId());
        userVO.setEmail(loginUser.getEmail());
        userVO.setName(loginUser.getName());
        userVO.setRole(loginUser.getRole());
        userVO.setAvatarUrl(loginUser.getAvatarUrl());
        userVO.setCreatedAt(loginUser.getCreatedAt());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", userVO);

        return Result.success("登录成功", responseData);
    }

    // 按视频ID查评论
    @GetMapping("/comment/byVideo/{videoId}")
    public Result<List<Comment>> getCommentsByVideoId(@PathVariable Long videoId) {
        return Result.success(adminService.getCommentsByVideoId(videoId));
    }
}
