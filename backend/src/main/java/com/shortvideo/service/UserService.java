package com.shortvideo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortvideo.dto.ChangePasswordDTO;
import com.shortvideo.dto.LoginDTO;
import com.shortvideo.dto.RegisterDTO;
import com.shortvideo.dto.UpdateProfileDTO;
import com.shortvideo.dto.UserVO;
import com.shortvideo.entity.User;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param registerDTO 注册参数（邮箱/密码/昵称）
     */
    void register(RegisterDTO registerDTO);
    
    /**
     * 用户登录
     *
     * @param loginDTO 登录参数（邮箱/密码）
     * @return 包含token和用户信息
     */
    Map<String, Object> login(LoginDTO loginDTO);
    
    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（脱敏，不包含密码）
     */
    UserVO getUserProfile(Long userId);

    UserVO updateProfile(Long userId, UpdateProfileDTO dto);

    void changePassword(Long userId, ChangePasswordDTO dto);
}
