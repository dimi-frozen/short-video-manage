package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortvideo.dto.ChangePasswordDTO;
import com.shortvideo.dto.LoginDTO;
import com.shortvideo.dto.RegisterDTO;
import com.shortvideo.dto.UpdateProfileDTO;
import com.shortvideo.dto.UserVO;
import com.shortvideo.entity.User;
import com.shortvideo.mapper.UserMapper;
import com.shortvideo.service.UserService;
import com.shortvideo.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册：
     * - 校验邮箱唯一性
     * - 使用 BCrypt 对密码加密后入库
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        // 1. 检查邮箱是否已存在
        User existingUser = this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, registerDTO.getEmail()));
        if (existingUser != null) {
            throw new RuntimeException("该邮箱已被注册");
        }
        
        // 2. 密码加密并保存
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setName(registerDTO.getName());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole("user");
        
        this.save(user);
    }

    /**
     * 用户登录：
     * - 校验邮箱是否存在
     * - 校验密码（BCrypt matches）
     * - 生成 JWT token 并返回
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 1. 根据邮箱查找用户
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, loginDTO.getEmail()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }
        
        // 3. 生成JWT
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        
        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        result.put("user", userVO);
        
        return result;
    }

    /**
     * 查询用户信息：
     * - 根据 userId 查询
     * - 返回 VO（不包含密码等敏感信息）
     */
    @Override
    public UserVO getUserProfile(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setName(dto.getName());
        user.setAvatarUrl(dto.getAvatarUrl());
        this.updateById(user);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
    }
}
