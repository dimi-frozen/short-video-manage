package com.shortvideo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResp<T> {
    private Long total;
    private List<T> list;
}

