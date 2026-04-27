package com.shortvideo.service.impl;

import com.shortvideo.dto.admin.PendingVideoVO;
import com.shortvideo.mapper.AdminVideoAuditMapper;
import com.shortvideo.service.AdminVideoAuditService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminVideoAuditServiceImpl implements AdminVideoAuditService {

    private final AdminVideoAuditMapper mapper;

    public AdminVideoAuditServiceImpl(AdminVideoAuditMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PendingVideoVO> listPending(int limit) {
        int lim = limit <= 0 ? 100 : Math.min(limit, 500);
        return mapper.listPending(lim);
    }

    @Override
    public void approve(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        int n = mapper.approve(id);
        if (n <= 0) {
            throw new IllegalArgumentException("视频不存在或已删除");
        }
    }

    @Override
    public void reject(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        int n = mapper.reject(id);
        if (n <= 0) {
            throw new IllegalArgumentException("视频不存在或已删除");
        }
    }
}

