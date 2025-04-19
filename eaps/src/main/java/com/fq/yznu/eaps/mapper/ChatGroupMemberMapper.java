package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.ChatGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天群组成员Mapper接口
 */
@Mapper
public interface ChatGroupMemberMapper extends BaseMapper<ChatGroupMember> {

    /**
     * 获取群组成员列表（包含用户基本信息）
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<Map<String, Object>> selectGroupMembersWithUserInfo(@Param("groupId") Long groupId);
    
    /**
     * 查询用户在群组中的角色
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 成员类型（0普通成员 1管理员 2群主）
     */
    @Select("SELECT member_type FROM chat_group_member WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 0")
    Integer selectMemberType(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    /**
     * 更新群组成员角色
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param memberType 成员类型
     * @return 影响行数
     */
    @Update("UPDATE chat_group_member SET member_type = #{memberType} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateMemberType(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("memberType") Integer memberType);
    
    /**
     * 更新群组成员禁言状态
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param muteEndTime 禁言结束时间
     * @return 影响行数
     */
    @Update("UPDATE chat_group_member SET mute_end_time = #{muteEndTime} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateMemberMuteStatus(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("muteEndTime") LocalDateTime muteEndTime);
    
    /**
     * 更新群组成员状态
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param status 状态（0正常 1已退出）
     * @return 影响行数
     */
    @Update("UPDATE chat_group_member SET status = #{status} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateMemberStatus(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 检查用户是否是群组成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否是成员
     */
    @Select("SELECT COUNT(*) FROM chat_group_member WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 0")
    int checkIsMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
} 