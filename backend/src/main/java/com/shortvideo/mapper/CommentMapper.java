package com.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortvideo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("<script>" +
            "SELECT video_id, COUNT(*) as count FROM comment " +
            "WHERE video_id IN " +
            "<foreach item='id' collection='videoIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "GROUP BY video_id" +
            "</script>")
    List<Map<String, Object>> batchCountCommentsByVideoIds(List<Long> videoIds);
}