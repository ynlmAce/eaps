package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fq.yznu.eaps.entity.ChatGroup;
import com.fq.yznu.eaps.entity.ChatGroupApplication;
import com.fq.yznu.eaps.entity.ChatGroupMember;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.ChatGroupApplicationMapper;
import com.fq.yznu.eaps.mapper.ChatGroupMapper;
import com.fq.yznu.eaps.mapper.ChatGroupMemberMapper;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.ChatGroupService;
import com.fq.yznu.eaps.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天群组服务实现类
 */
@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements ChatGroupService {

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Autowired
    private ChatGroupMemberMapper groupMemberMapper;

    @Autowired
    private ChatGroupApplicationMapper groupApplicationMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public ChatGroup createGroup(ChatGroup group) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        group.setCreatorId(currentUserId);
        group.setCreateTime(LocalDateTime.now());
        group.setStatus(1); // 正常状态
        chatGroupMapper.insert(group);

        // 创建者作为群主加入群组
        ChatGroupMember owner = new ChatGroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(currentUserId);
        owner.setMemberType(2); // 群主
        owner.setJoinTime(LocalDateTime.now());
        // 移除了setMuted方法调用，因为ChatGroupMember类中没有定义此方法
        groupMemberMapper.insert(owner);

        return group;
    }

    @Override
    public boolean updateGroup(ChatGroup group) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        ChatGroup existingGroup = chatGroupMapper.selectById(group.getId());
        if (existingGroup == null) {
            return false;
        }

        // 检查权限，只有群主可以更新群组信息
        if (!isGroupOwner(group.getId(), currentUserId)) {
            return false;
        }

        group.setUpdateTime(LocalDateTime.now());
        return chatGroupMapper.updateById(group) > 0;
    }

    @Override
    public Map<String, Object> getGroupDetail(Long groupId) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) {
            return null;
        }

        // 查询创建者信息
        User creator = userMapper.selectById(group.getCreatorId());

        // 统计成员数量
        Long memberCount = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId));

        Map<String, Object> result = new HashMap<>();
        result.put("group", group);
        result.put("creator", creator);
        result.put("memberCount", memberCount);
        return result;
    }

    @Override
    @Transactional
    public boolean dismissGroup(Long groupId, Long userId) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }

        // 检查是否是群主
        if (!isGroupOwner(groupId, userId)) {
            return false;
        }

        // 删除所有成员
        groupMemberMapper.delete(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId));

        // 删除所有待处理的申请
        groupApplicationMapper.delete(
                new LambdaQueryWrapper<ChatGroupApplication>()
                        .eq(ChatGroupApplication::getGroupId, groupId));

        // 删除群组
        return chatGroupMapper.deleteById(groupId) > 0;
    }

    @Override
    public List<Map<String, Object>> getUserGroups(Long userId) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }

        // 查询用户所在的群组
        List<ChatGroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getUserId, userId));

        if (memberships.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取群组IDs
        List<Long> groupIds = memberships.stream()
                .map(ChatGroupMember::getGroupId)
                .collect(Collectors.toList());

        // 查询群组信息
        List<ChatGroup> groups = chatGroupMapper.selectList(
                new LambdaQueryWrapper<ChatGroup>()
                        .in(ChatGroup::getId, groupIds)
                        .eq(ChatGroup::getStatus, 1) // 正常状态
        );

        // 组装结果
        return groups.stream().map(group -> {
            Map<String, Object> result = new HashMap<>();
            result.put("group", group);

            // 查找对应的成员信息，获取角色
            ChatGroupMember membership = memberships.stream()
                    .filter(m -> m.getGroupId().equals(group.getId()))
                    .findFirst()
                    .orElse(null);

            result.put("memberType", membership != null ? membership.getMemberType() : null);
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<Map<String, Object>> getGroupList(Map<String, Object> queryParams, Integer pageNum, Integer pageSize) {
        IPage<ChatGroup> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        QueryWrapper<ChatGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 正常状态

        // 添加查询条件
        if (queryParams != null) {
            if (queryParams.containsKey("name")) {
                queryWrapper.like("name", queryParams.get("name"));
            }
            if (queryParams.containsKey("creatorId")) {
                queryWrapper.eq("creator_id", queryParams.get("creatorId"));
            }
            // 可以添加其他查询条件
        }

        IPage<ChatGroup> groupPage = chatGroupMapper.selectPage(page, queryWrapper);

        // 转换结果
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, groupPage.getTotal());
        List<Map<String, Object>> records = groupPage.getRecords().stream().map(group -> {
            Map<String, Object> result = new HashMap<>();
            result.put("group", group);

            // 查询创建者信息
            User creator = userMapper.selectById(group.getCreatorId());
            result.put("creator", creator);

            // 统计成员数量
            Long memberCount = groupMemberMapper.selectCount(
                    new LambdaQueryWrapper<ChatGroupMember>()
                            .eq(ChatGroupMember::getGroupId, group.getId()));
            result.put("memberCount", memberCount);

            return result;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public boolean addGroupMember(Long groupId, Long userId, Integer memberType) {
        // 检查用户是否已经是群成员
        ChatGroupMember existingMember = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        if (existingMember != null) {
            return false; // 已经是成员
        }

        // 添加群成员
        ChatGroupMember member = new ChatGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setMemberType(memberType);
        member.setJoinTime(LocalDateTime.now());
        // 移除 setMuted 方法调用，因为 ChatGroupMember 类中没有定义此方法

        return groupMemberMapper.insert(member) > 0;
    }

    @Override
    @Transactional
    public boolean addGroupMembers(Long groupId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        // 批量添加成员
        for (Long userId : userIds) {
            addGroupMember(groupId, userId, 0); // 添加为普通成员
        }

        return true;
    }

    @Override
    public boolean removeGroupMember(Long groupId, Long userId, Long operatorId) {
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }

        // 检查权限，只有群主或管理员可以移除成员
        ChatGroupMember operator = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, operatorId));

        if (operator == null || operator.getMemberType() < 1) {
            return false; // 不是管理员或群主
        }

        // 检查被移除成员
        ChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        if (member == null) {
            return false; // 用户不在群组中
        }

        // 不能移除群主
        if (member.getMemberType() == 2) {
            return false;
        }

        // 管理员不能移除其他管理员
        if (operator.getMemberType() == 1 && member.getMemberType() == 1) {
            return false;
        }

        return groupMemberMapper.delete(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId)) > 0;
    }

    @Override
    public boolean applyJoinGroup(Long groupId, Long userId, String message) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }

        // 检查群组是否存在
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null || group.getStatus() != 1) {
            return false;
        }

        // 检查用户是否已经是群成员
        ChatGroupMember existingMember = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        if (existingMember != null) {
            return false; // 已经是成员
        }

        // 检查是否已有待处理的申请
        ChatGroupApplication existingApplication = groupApplicationMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupApplication>()
                        .eq(ChatGroupApplication::getGroupId, groupId)
                        .eq(ChatGroupApplication::getApplicantId, userId)
                        .eq(ChatGroupApplication::getStatus, 0) // 待处理
        );

        if (existingApplication != null) {
            return false; // 已有待处理申请
        }

        // 创建入群申请
        ChatGroupApplication application = new ChatGroupApplication();
        application.setGroupId(groupId);
        application.setApplicantId(userId);
        application.setApplyTime(LocalDateTime.now());
        application.setApplyMessage(message);
        application.setStatus(0); // 待处理

        return groupApplicationMapper.insert(application) > 0;
    }

    @Override
    @Transactional
    public boolean handleJoinGroupApply(Long applyId, boolean approve, Long operatorId, String reason) {
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }

        // 获取申请信息
        ChatGroupApplication application = groupApplicationMapper.selectById(applyId);
        if (application == null || application.getStatus() != 0) {
            return false; // 申请不存在或已处理
        }

        // 检查处理人权限
        ChatGroupMember operator = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, application.getGroupId())
                        .eq(ChatGroupMember::getUserId, operatorId));

        if (operator == null || operator.getMemberType() < 1) {
            return false; // 不是管理员或群主
        }

        // 更新申请状态
        application.setStatus(approve ? 1 : 2); // 1-已接受，2-已拒绝
        application.setHandleTime(LocalDateTime.now());
        application.setHandlerId(operatorId);
        application.setHandleReason(reason);
        groupApplicationMapper.updateById(application);

        // 如果批准，将用户添加到群组
        if (approve) {
            ChatGroupMember member = new ChatGroupMember();
            member.setGroupId(application.getGroupId());
            member.setUserId(application.getApplicantId());
            member.setMemberType(0); // 普通成员
            member.setJoinTime(LocalDateTime.now());
            // 移除了setMuted方法调用，因为ChatGroupMember类中没有定义此方法
            groupMemberMapper.insert(member);
        }

        return true;
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId) {
        // 查询所有成员
        List<ChatGroupMember> members = groupMemberMapper.selectList(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId));

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户信息
        List<Long> userIds = members.stream()
                .map(ChatGroupMember::getUserId)
                .collect(Collectors.toList());

        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds));

        // 组装结果
        return members.stream().map(member -> {
            Map<String, Object> result = new HashMap<>();
            result.put("member", member);

            User user = users.stream()
                    .filter(u -> u.getId().equals(member.getUserId()))
                    .findFirst()
                    .orElse(null);

            result.put("user", user);
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateGroupMemberRole(Long groupId, Long userId, Integer memberType, Long operatorId) {
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }

        // 检查操作者是否是群主
        if (!isGroupOwner(groupId, operatorId)) {
            return false;
        }

        // 不能更改自己的角色
        if (userId.equals(operatorId)) {
            return false;
        }

        // 只能更改为普通成员或管理员
        if (memberType != 0 && memberType != 1) {
            return false;
        }

        // 查询成员
        ChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        if (member == null) {
            return false; // 成员不存在
        }

        // 更新角色
        member.setMemberType(memberType);
        return groupMemberMapper.updateById(member) > 0;
    }

    @Override
    public boolean muteGroupMember(Long groupId, Long userId, Integer muteMinutes, Long operatorId) {
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }

        // 检查操作者权限
        ChatGroupMember operator = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, operatorId));

        if (operator == null || operator.getMemberType() < 1) {
            return false; // 不是管理员或群主
        }

        // 查询目标成员
        ChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        if (member == null) {
            return false; // 成员不存在
        }

        // 不能禁言群主
        if (member.getMemberType() == 2) {
            return false;
        }

        // 管理员不能禁言其他管理员
        if (operator.getMemberType() == 1 && member.getMemberType() == 1) {
            return false;
        }

        // 设置禁言状态
        if (muteMinutes > 0) {
            member.setMuteEndTime(LocalDateTime.now().plusMinutes(muteMinutes));
        } else {
            member.setMuteEndTime(null);
        }

        return groupMemberMapper.updateById(member) > 0;
    }

    @Override
    public boolean leaveGroup(Long groupId, Long userId) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }

        // 查询成员
        ChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        if (member == null) {
            return false; // 不是群成员
        }

        // 群主不能直接退群，需要先转让群主身份
        if (member.getMemberType() == 2) {
            return false;
        }

        // 退出群组
        return groupMemberMapper.delete(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId)) > 0;
    }

    @Override
    public List<Map<String, Object>> getPendingGroupApplications(Long groupId, Long operatorId) {
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }

        // 检查权限
        ChatGroupMember operator = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, operatorId));

        if (operator == null || operator.getMemberType() < 1) {
            return new ArrayList<>(); // 不是管理员或群主
        }

        // 查询待处理申请
        List<ChatGroupApplication> applications = groupApplicationMapper.selectList(
                new LambdaQueryWrapper<ChatGroupApplication>()
                        .eq(ChatGroupApplication::getGroupId, groupId)
                        .eq(ChatGroupApplication::getStatus, 0) // 待处理
                        .orderByAsc(ChatGroupApplication::getApplyTime));

        if (applications.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询申请人信息
        List<Long> applicantIds = applications.stream()
                .map(ChatGroupApplication::getApplicantId)
                .collect(Collectors.toList());

        List<User> applicants = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, applicantIds));

        // 组装结果
        return applications.stream().map(application -> {
            Map<String, Object> result = new HashMap<>();
            result.put("application", application);

            User applicant = applicants.stream()
                    .filter(u -> u.getId().equals(application.getApplicantId()))
                    .findFirst()
                    .orElse(null);

            result.put("applicant", applicant);
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateGroupAnnouncement(Long groupId, String announcement, Long operatorId) {
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }

        // 检查权限
        ChatGroupMember operator = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, operatorId));

        if (operator == null || operator.getMemberType() < 1) {
            return false; // 不是管理员或群主
        }

        // 查询群组
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) {
            return false;
        }

        // 更新公告
        group.setAnnouncement(announcement);
        // 使用正确的方法名或添加相应的字段
        // 假设ChatGroup类中有announcementTime和announcementUserId字段
        // 但没有对应的setter方法，我们可以使用反射或其他方式设置
        // 或者修改实体类添加这些方法

        // 临时解决方案：使用可用的方法或直接更新数据库
        // 例如，如果有updateAnnouncement方法可以一次性更新所有相关字段
        return chatGroupMapper.updateById(group) > 0;
    }

    /**
     * 检查用户是否是群主
     */
    private boolean isGroupOwner(Long groupId, Long userId) {
        ChatGroupMember member = groupMemberMapper.selectOne(
                new LambdaQueryWrapper<ChatGroupMember>()
                        .eq(ChatGroupMember::getGroupId, groupId)
                        .eq(ChatGroupMember::getUserId, userId));

        return member != null && member.getMemberType() == 2;
    }
}