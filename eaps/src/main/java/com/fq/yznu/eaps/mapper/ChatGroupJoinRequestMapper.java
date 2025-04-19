package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.ChatGroupJoinRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 聊天群组加入申请Mapper接口
 */
@Mapper
public interface ChatGroupJoinRequestMapper extends BaseMapper<ChatGroupJoinRequest> {

    /**
     * 获取群组的待处理申请列表
     *
     * @param groupId 群组ID
     * @return 申请列表
     */
    List<Map<String, Object>> selectPendingRequests(@Param("groupId") Long groupId);
    
    /**
     * 检查用户是否有待处理的申请
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否存在待处理申请
     */
    @Select("SELECT COUNT(*) FROM chat_group_join_request WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 0")
    int checkPendingRequest(@Param("groupId") Long groupId, @Param("userId") Long userId);
} 