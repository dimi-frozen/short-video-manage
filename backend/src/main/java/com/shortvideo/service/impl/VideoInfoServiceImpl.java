package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortvideo.entity.VideoInfo;
import com.shortvideo.mapper.VideoInfoMapper;
import com.shortvideo.service.VideoInfoService;
import org.springframework.stereotype.Service;

@Service
public class VideoInfoServiceImpl extends ServiceImpl<VideoInfoMapper, VideoInfo> implements VideoInfoService {
}

