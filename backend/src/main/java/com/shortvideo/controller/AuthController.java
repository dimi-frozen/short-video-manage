package com.shortvideo.controller;

import com.shortvideo.dto.LoginDTO;
import com.shortvideo.dto.RegisterDTO;
import com.shortvideo.service.UserService;
import com.shortvideo.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@Validated @RequestBody RegisterDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = userService.login(loginDTO);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
