package com.shortvideo.dto.publish;

import lombok.Data;

@Data
public class PublishConfirmResp {
    private Long recordId;
    private String status;
    private String platformVideoUrl;
    private String platformVideoId;
}

