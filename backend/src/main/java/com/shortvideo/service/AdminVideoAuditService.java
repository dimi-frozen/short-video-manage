package com.shortvideo.service;

import com.shortvideo.dto.admin.PendingVideoVO;

import java.util.List;

public interface AdminVideoAuditService {
    List<PendingVideoVO> listPending(int limit);

    void approve(Long id);

    void reject(Long id);
}

