package com.shortvideo.dto.publish;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PublishReq {
    @NotNull
    private Long videoId;

    @NotBlank
    private String platform;
}

