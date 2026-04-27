package com.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortvideo.dto.analysis.AnalysisOverviewVO;
import com.shortvideo.dto.analysis.AnalysisPlatformAggVO;
import com.shortvideo.dto.analysis.AnalysisTopVideoVO;
import com.shortvideo.dto.analysis.AnalysisTrendPointVO;
import com.shortvideo.entity.VideoInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VideoInfoMapper extends BaseMapper<VideoInfo> {

    @Select({
            "<script>",
            "select",
            "count(1) as totalVideos,",
            "coalesce(sum(views),0) as totalViews,",
            "coalesce(sum(likes),0) as totalLikes,",
            "coalesce(sum(comments),0) as totalComments,",
            "coalesce(sum(shares),0) as totalShares",
            "from video_info",
            "where deleted = 0",
            "<if test='platformType != null and platformType != \"\"'>",
            "and platform_type = #{platformType}",
            "</if>",
            "<if test='startTime != null'>",
            "and publish_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and publish_time &lt;= #{endTime}",
            "</if>",
            "</script>"
    })
    AnalysisOverviewVO selectOverview(@Param("platformType") String platformType,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Select({
            "<script>",
            "select",
            "platform_type as platformType,",
            "count(1) as videoCount,",
            "coalesce(sum(views),0) as views,",
            "coalesce(sum(likes),0) as likes,",
            "coalesce(sum(comments),0) as comments,",
            "coalesce(sum(shares),0) as shares",
            "from video_info",
            "where deleted = 0",
            "<if test='startTime != null'>",
            "and publish_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and publish_time &lt;= #{endTime}",
            "</if>",
            "group by platform_type",
            "order by views desc",
            "</script>"
    })
    List<AnalysisPlatformAggVO> selectPlatformAgg(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    @Select({
            "<script>",
            "select",
            "video_id as videoId,",
            "title as title,",
            "platform_type as platformType,",
            "publish_time as publishTime,",
            "views as views,",
            "likes as likes,",
            "comments as comments,",
            "shares as shares",
            "from video_info",
            "where deleted = 0",
            "<if test='platformType != null and platformType != \"\"'>",
            "and platform_type = #{platformType}",
            "</if>",
            "<if test='startTime != null'>",
            "and publish_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and publish_time &lt;= #{endTime}",
            "</if>",
            "<choose>",
            "<when test='orderBy == \"likes\"'>order by likes desc</when>",
            "<when test='orderBy == \"comments\"'>order by comments desc</when>",
            "<when test='orderBy == \"shares\"'>order by shares desc</when>",
            "<otherwise>order by views desc</otherwise>",
            "</choose>",
            "limit #{limit}",
            "</script>"
    })
    List<AnalysisTopVideoVO> selectTopVideos(@Param("platformType") String platformType,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("orderBy") String orderBy,
                                            @Param("limit") int limit);

    @Select({
            "<script>",
            "select",
            "date_format(publish_time, '%Y-%m-%d') as day,",
            "coalesce(sum(views),0) as views,",
            "coalesce(sum(likes),0) as likes,",
            "coalesce(sum(comments),0) as comments,",
            "coalesce(sum(shares),0) as shares",
            "from video_info",
            "where deleted = 0",
            "<if test='platformType != null and platformType != \"\"'>",
            "and platform_type = #{platformType}",
            "</if>",
            "<if test='startTime != null'>",
            "and publish_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and publish_time &lt;= #{endTime}",
            "</if>",
            "group by date_format(publish_time, '%Y-%m-%d')",
            "order by day asc",
            "</script>"
    })
    List<AnalysisTrendPointVO> selectTrend(@Param("platformType") String platformType,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}
