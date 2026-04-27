package com.shortvideo.dto;

import lombok.Data;

import java.util.List;

@Data
public class VideoUpdateReq {
    private String title;
    private String description;
    private List<String> tags;
}

