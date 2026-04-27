package com.shortvideo.controller;

import com.shortvideo.dto.UserVO;
import com.shortvideo.dto.ChangePasswordDTO;
import com.shortvideo.dto.UpdateProfileDTO;
import com.shortvideo.service.UserService;
import com.shortvideo.utils.JwtUtils;
import com.shortvideo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile(HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            UserVO userVO = userService.getUserProfile(userId);
            return Result.success(userVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Validated @RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            UserVO vo = userService.updateProfile(userId, dto);
            return Result.success("保存成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Validated @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            userService.changePassword(userId, dto);
            return Result.success("修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private Long requireUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("token");
        }
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("未登录或Token失效");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null || jwtUtils.isTokenExpired(token)) {
            throw new RuntimeException("未登录或Token失效");
        }
        return userId;
    }
}
