package com.shortvideo.dto.publish;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PublishConfirmReq {
    @NotNull
    private Long recordId;
}

