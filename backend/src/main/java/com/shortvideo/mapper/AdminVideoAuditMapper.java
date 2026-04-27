package com.shortvideo.mapper;

import com.shortvideo.dto.admin.PendingVideoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminVideoAuditMapper {

    @Select("SELECT " +
            "id, " +
            "title, " +
            "description, " +
            "tags, " +
            "file_path AS fileUrl, " +
            "cover_url AS coverUrl, " +
            "file_size AS fileSize, " +
            "status AS transcodeStatus, " +
            "audit_status AS auditStatus, " +
            "create_time AS createTime " +
            "FROM sv_video " +
            "WHERE deleted = 0 AND audit_status = 'PENDING' " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<PendingVideoVO> listPending(@Param("limit") int limit);

    @Update("UPDATE sv_video SET audit_status = 'APPROVED', update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int approve(@Param("id") Long id);

    @Update("UPDATE sv_video SET audit_status = 'REJECTED', update_time = NOW() WHERE id = #{id} AND deleted = 0")
    int reject(@Param("id") Long id);
}

