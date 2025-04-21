package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fq.yznu.eaps.entity.ChatGroup;

import java.util.List;
import java.util.Map;

/**
 * 聊天群组服务接口
 */
public interface ChatGroupService extends IService<ChatGroup> {

    /**
     * 创建群组
     *
     * @param group 群组信息
     * @return 创建后的群组信息
     */
    ChatGroup createGroup(ChatGroup group);

    /**
     * 更新群组信息
     *
     * @param group 群组信息
     * @return 是否更新成功
     */
    boolean updateGroup(ChatGroup group);

    /**
     * 获取群组详情
     *
     * @param groupId 群组ID
     * @return 群组详情，包含创建者信息和成员数量
     */
    Map<String, Object> getGroupDetail(Long groupId);

    /**
     * 解散群组，仅群主可操作
     *
     * @param groupId 群组ID
     * @param userId  操作用户ID，如果为null则使用当前登录用户
     * @return 是否解散成功
     */
    boolean dismissGroup(Long groupId, Long userId);

    /**
     * 获取用户所加入的群组列表
     *
     * @param userId 用户ID，如果为null则使用当前登录用户
     * @return 群组列表
     */
    List<Map<String, Object>> getUserGroups(Long userId);

    /**
     * 分页获取群组列表
     *
     * @param queryParams 查询参数
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 分页结果
     */
    Page<Map<String, Object>> getGroupList(Map<String, Object> queryParams, Integer pageNum, Integer pageSize);

    /**
     * 向群组添加一个成员
     *
     * @param groupId    群组ID
     * @param userId     用户ID
     * @param memberType 成员类型：0-普通成员，1-管理员，2-群主
     * @return 是否添加成功
     */
    boolean addGroupMember(Long groupId, Long userId, Integer memberType);

    /**
     * 批量添加群组成员
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 是否添加成功
     */
    boolean addGroupMembers(Long groupId, List<Long> userIds);

    /**
     * 移除群组成员，仅群主或管理员可操作
     *
     * @param groupId    群组ID
     * @param userId     被移除的用户ID
     * @param operatorId 操作者ID，如果为null则使用当前登录用户
     * @return 是否移除成功
     */
    boolean removeGroupMember(Long groupId, Long userId, Long operatorId);

    /**
     * 申请加入群组
     *
     * @param groupId 群组ID
     * @param userId  申请人ID，如果为null则使用当前登录用户
     * @param message 申请消息
     * @return 是否申请成功
     */
    boolean applyJoinGroup(Long groupId, Long userId, String message);

    /**
     * 处理入群申请
     *
     * @param applyId    申请ID
     * @param approve    是否批准
     * @param operatorId 处理人ID，如果为null则使用当前登录用户
     * @param reason     拒绝原因，仅在不批准时有效
     * @return 是否处理成功
     */
    boolean handleJoinGroupApply(Long applyId, boolean approve, Long operatorId, String reason);

    /**
     * 获取群组成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表，包含用户基本信息
     */
    List<Map<String, Object>> getGroupMembers(Long groupId);

    /**
     * 更新群组成员角色，仅群主可操作
     *
     * @param groupId    群组ID
     * @param userId     目标用户ID
     * @param memberType 新的成员类型：0-普通成员，1-管理员
     * @param operatorId 操作者ID，如果为null则使用当前登录用户
     * @return 是否更新成功
     */
    boolean updateGroupMemberRole(Long groupId, Long userId, Integer memberType, Long operatorId);

    /**
     * 设置群成员禁言，仅群主或管理员可操作
     *
     * @param groupId     群组ID
     * @param userId      目标用户ID
     * @param muteMinutes 禁言时长(分钟)，0表示解除禁言
     * @param operatorId  操作者ID，如果为null则使用当前登录用户
     * @return 是否设置成功
     */
    boolean muteGroupMember(Long groupId, Long userId, Integer muteMinutes, Long operatorId);

    /**
     * 退出群组
     *
     * @param groupId 群组ID
     * @param userId  用户ID，如果为null则使用当前登录用户
     * @return 是否退出成功
     */
    boolean leaveGroup(Long groupId, Long userId);

    /**
     * 获取待处理的入群申请
     *
     * @param groupId    群组ID
     * @param operatorId 操作者ID，如果为null则使用当前登录用户，需要是群主或管理员
     * @return 申请列表
     */
    List<Map<String, Object>> getPendingGroupApplications(Long groupId, Long operatorId);

    /**
     * 更新群组公告，仅群主或管理员可操作
     *
     * @param groupId      群组ID
     * @param announcement 公告内容
     * @param operatorId   操作者ID，如果为null则使用当前登录用户
     * @return 是否更新成功
     */
    boolean updateGroupAnnouncement(Long groupId, String announcement, Long operatorId);
}