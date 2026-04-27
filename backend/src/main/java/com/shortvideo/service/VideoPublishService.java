package com.shortvideo.service;

import com.shortvideo.dto.publish.PublishConfirmResp;
import com.shortvideo.dto.publish.PublishRecordResp;
import com.shortvideo.dto.publish.PublishResp;

import java.util.List;

public interface VideoPublishService {
    PublishResp createPublish(Long videoId, String platform);

    PublishConfirmResp confirmPublish(Long recordId);

    PublishRecordResp getRecord(Long recordId);

    List<PublishRecordResp> listRecords(Long videoId, String platform);
}
