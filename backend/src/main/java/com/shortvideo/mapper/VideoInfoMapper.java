package com.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortvideo.dto.analysis.AnalysisOverviewVO;
import com.shortvideo.dto.analysis.AnalysisPlatformAggVO;
import com.shortvideo.dto.analysis.AnalysisTopVideoVO;
import com.shortvideo.dto.analysis.AnalysisTrendPointVO;
import com.shortvideo.entity.VideoInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VideoInfoMapper extends BaseMapper<VideoInfo> {

    /**
     * 查询总体统计数据
     */
    AnalysisOverviewVO selectOverview(@Param("platformType") String platformType,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询各平台聚合数据
     */
    List<AnalysisPlatformAggVO> selectPlatformAgg(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询Top视频列表
     */
    List<AnalysisTopVideoVO> selectTopVideos(@Param("platformType") String platformType,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("orderBy") String orderBy,
                                            @Param("limit") int limit);

    /**
     * 查询趋势数据（按天统计）
     */
    List<AnalysisTrendPointVO> selectTrend(@Param("platformType") String platformType,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}
