package com.shortvideo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateProfileDTO {
    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 32, message = "昵称长度需在1-32字符之间")
    private String name;

    @Size(max = 512, message = "头像地址长度过长")
    private String avatarUrl;
}

