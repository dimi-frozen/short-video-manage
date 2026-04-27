package com.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortvideo.dto.analysis.LocalTrendPointVO;
import com.shortvideo.entity.SvVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SvVideoMapper extends BaseMapper<SvVideo> {

    /**
     * 查询本平台视频趋势（按天统计）
     */
    @Select({
            "<script>",
            "select",
            "date_format(v.create_time, '%Y-%m-%d') as day,",
            "count(distinct v.id) as uploadCount,",
            "coalesce(sum(v.views),0) as views,",
            "coalesce(sum(v.likes),0) as likes",
            "from sv_video v",
            "where v.deleted = 0",
            "<if test='startTime != null'>",
            "and v.create_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and v.create_time &lt;= #{endTime}",
            "</if>",
            "group by date_format(v.create_time, '%Y-%m-%d')",
            "order by day asc",
            "</script>"
    })
    List<LocalTrendPointVO> selectLocalTrend(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询本平台每日评论数趋势
     */
    @Select({
            "<script>",
            "select",
            "date_format(v.create_time, '%Y-%m-%d') as day,",
            "count(c.id) as comments",
            "from comment c",
            "inner join sv_video v on c.video_id = v.id",
            "where v.deleted = 0",
            "<if test='startTime != null'>",
            "and v.create_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and v.create_time &lt;= #{endTime}",
            "</if>",
            "group by date_format(v.create_time, '%Y-%m-%d')",
            "order by day asc",
            "</script>"
    })
    List<LocalTrendPointVO> selectLocalCommentTrend(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询本平台视频总数
     */
    @Select({
            "<script>",
            "select count(1)",
            "from sv_video",
            "where deleted = 0",
            "<if test='startTime != null'>",
            "and create_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and create_time &lt;= #{endTime}",
            "</if>",
            "</script>"
    })
    Long selectLocalVideoCount(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询本平台总播放量
     */
    @Select({
            "<script>",
            "select coalesce(sum(views), 0)",
            "from sv_video",
            "where deleted = 0",
            "<if test='startTime != null'>",
            "and create_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and create_time &lt;= #{endTime}",
            "</if>",
            "</script>"
    })
    Long selectLocalTotalViews(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询本平台总点赞数
     */
    @Select({
            "<script>",
            "select coalesce(sum(likes), 0)",
            "from sv_video",
            "where deleted = 0",
            "<if test='startTime != null'>",
            "and create_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and create_time &lt;= #{endTime}",
            "</if>",
            "</script>"
    })
    Long selectLocalTotalLikes(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询本平台总评论数（通过关联comment表）
     */
    @Select({
            "<script>",
            "select count(c.id)",
            "from comment c",
            "inner join sv_video v on c.video_id = v.id",
            "where v.deleted = 0",
            "<if test='startTime != null'>",
            "and v.create_time &gt;= #{startTime}",
            "</if>",
            "<if test='endTime != null'>",
            "and v.create_time &lt;= #{endTime}",
            "</if>",
            "</script>"
    })
    Long selectLocalTotalComments(@Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);
}