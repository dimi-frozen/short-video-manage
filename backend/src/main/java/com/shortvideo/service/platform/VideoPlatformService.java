package com.shortvideo.service.platform;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortvideo.dto.platform.AuthUrlResp;
import com.shortvideo.dto.platform.PublishResp;
import com.shortvideo.dto.platform.VideoListItemResp;
import com.shortvideo.dto.platform.VideoStatsResp;
import com.shortvideo.enums.PlatformType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public interface VideoPlatformService {
    PlatformType platformType();

    AuthUrlResp getAuthUrl();

    PublishResp publish(MultipartFile file, String title, String accessToken);

    PublishResp publishByUrl(String fileUrl, Long fileSize, String title, String accessToken);

    VideoStatsResp stats(String videoId);

    IPage<VideoListItemResp> page(int pageNum, int pageSize, String sortBy, String sortOrder, LocalDateTime startTime, LocalDateTime endTime);

    void updateTitle(String videoId, String title);

    void delete(String videoId);
}
