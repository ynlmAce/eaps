package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.ChatGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 聊天群组Mapper接口
 */
@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 分页查询群组列表
     *
     * @param page 分页参数
     * @param params 查询参数
     * @return 群组列表
     */
    IPage<ChatGroup> selectGroupPage(Page<ChatGroup> page, @Param("params") Map<String, Object> params);
    
    /**
     * 获取用户加入的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<Map<String, Object>> selectUserGroups(@Param("userId") Long userId);
    
    /**
     * 获取群组详情（包含创建者信息）
     *
     * @param groupId 群组ID
     * @return 群组详情
     */
    Map<String, Object> selectGroupDetail(@Param("groupId") Long groupId);
    
    /**
     * 更新群组成员数量
     *
     * @param groupId 群组ID
     * @param count 增加或减少的数量（正数表示增加，负数表示减少）
     * @return 影响行数
     */
    @Update("UPDATE chat_group SET current_member_count = current_member_count + #{count} WHERE id = #{groupId}")
    int updateMemberCount(@Param("groupId") Long groupId, @Param("count") int count);
    
    /**
     * 更新群组公告
     *
     * @param groupId 群组ID
     * @param announcement 公告内容
     * @return 影响行数
     */
    @Update("UPDATE chat_group SET announcement = #{announcement} WHERE id = #{groupId}")
    int updateAnnouncement(@Param("groupId") Long groupId, @Param("announcement") String announcement);
} 