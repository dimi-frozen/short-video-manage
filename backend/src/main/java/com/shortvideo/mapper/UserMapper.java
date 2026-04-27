package com.shortvideo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortvideo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("<script>" +
            "SELECT id, name FROM users WHERE id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> batchSelectUsersByIds(List<Long> userIds);
}
