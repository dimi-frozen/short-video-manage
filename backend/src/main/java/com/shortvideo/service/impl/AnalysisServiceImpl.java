package com.shortvideo.service.impl;

import com.shortvideo.dto.analysis.AnalysisDashboardVO;
import com.shortvideo.dto.analysis.AnalysisOverviewVO;
import com.shortvideo.dto.analysis.AnalysisPlatformAggVO;
import com.shortvideo.dto.analysis.AnalysisTopVideoVO;
import com.shortvideo.dto.analysis.AnalysisTrendPointVO;
import com.shortvideo.mapper.VideoInfoMapper;
import com.shortvideo.service.AnalysisService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final VideoInfoMapper videoInfoMapper;

    public AnalysisServiceImpl(VideoInfoMapper videoInfoMapper) {
        this.videoInfoMapper = videoInfoMapper;
    }

    @Override
    public AnalysisDashboardVO dashboard(String platformType, String orderBy, int topLimit, LocalDateTime startTime, LocalDateTime endTime) {
        String pt = StringUtils.hasText(platformType) ? platformType.trim().toUpperCase() : null;
        String ob = StringUtils.hasText(orderBy) ? orderBy.trim().toLowerCase() : "views";
        int limit = topLimit <= 0 ? 10 : Math.min(topLimit, 50);

        AnalysisOverviewVO overview = videoInfoMapper.selectOverview(pt, startTime, endTime);
        List<AnalysisPlatformAggVO> platformAgg = videoInfoMapper.selectPlatformAgg(startTime, endTime);
        List<AnalysisTopVideoVO> topVideos = videoInfoMapper.selectTopVideos(pt, startTime, endTime, ob, limit);
        List<AnalysisTrendPointVO> trend = videoInfoMapper.selectTrend(pt, startTime, endTime);

        AnalysisDashboardVO vo = new AnalysisDashboardVO();
        vo.setOverview(overview);
        vo.setPlatformAgg(platformAgg);
        vo.setTopVideos(topVideos);
        vo.setTrend(trend);
        return vo;
    }
}

