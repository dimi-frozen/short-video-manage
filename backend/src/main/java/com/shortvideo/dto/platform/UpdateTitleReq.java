package com.shortvideo.dto.platform;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateTitleReq {
    @NotBlank(message = "title不能为空")
    @Size(min = 1, max = 128, message = "标题长度需在1-128字符之间")
    private String title;
}

