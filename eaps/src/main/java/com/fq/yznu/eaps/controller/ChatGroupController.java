package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.ChatGroup;
import com.fq.yznu.eaps.service.ChatGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 聊天群组控制器
 */
@Tag(name = "聊天群组管理")
@RestController
@RequestMapping("/chat/group")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ChatGroupController {

    private final ChatGroupService chatGroupService;

    /**
     * 创建群组
     *
     * @param group      群组信息
     * @param avatarFile 群组头像（可选）
     * @return 创建的群组
     */
    @Operation(summary = "创建群组")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<ChatGroup> createGroup(
            @Parameter(description = "群组信息") @RequestPart("group") @Valid ChatGroup group,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {

        ChatGroup result = chatGroupService.createGroup(group);
        return ResponseResult.success(result);
    }

    /**
     * 更新群组信息
     *
     * @param group      群组信息
     * @param avatarFile 群组头像（可选）
     * @return 操作结果
     */
    @Operation(summary = "更新群组信息")
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> updateGroup(
            @RequestPart("group") @Valid ChatGroup group,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {

        boolean result = chatGroupService.updateGroup(group);
        return result ? ResponseResult.success() : ResponseResult.error("更新失败");
    }

    /**
     * 获取群组详情
     *
     * @param groupId 群组ID
     * @return 群组详情
     */
    @Operation(summary = "获取群组信息")
    @GetMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Map<String, Object>> getGroupDetail(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId) {
        Map<String, Object> detail = chatGroupService.getGroupDetail(groupId);
        return ResponseResult.success(detail);
    }

    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @return 操作结果
     */
    @Operation(summary = "解散群组")
    @DeleteMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> dismissGroup(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId) {
        boolean result = chatGroupService.dismissGroup(groupId, null); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("解散失败，可能没有权限");
    }

    /**
     * 获取我加入的群组
     *
     * @return 群组列表
     */
    @Operation(summary = "获取我的群组列表")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<List<Map<String, Object>>> getMyGroups() {
        List<Map<String, Object>> groups = chatGroupService.getUserGroups(null); // null表示当前登录用户
        return ResponseResult.success(groups);
    }

    /**
     * 分页查询群组列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param params   查询参数
     * @return 群组列表
     */
    @Operation(summary = "查询群组列表")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Page<Map<String, Object>>> getGroupList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 50, message = "每页条数最大为50") Integer pageSize,
            @RequestParam(required = false) Map<String, Object> params) {

        Page<Map<String, Object>> page = chatGroupService.getGroupList(params, pageNum, pageSize);
        return ResponseResult.success(page);
    }

    /**
     * 邀请用户加入群组
     *
     * @param groupId 群组ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "邀请用户加入群组")
    @PostMapping("/{groupId}/invite")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> inviteToGroup(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId,
            @RequestBody @NotNull(message = "用户ID列表不能为空") List<Long> userIds) {

        boolean result = chatGroupService.addGroupMembers(groupId, userIds);
        return result ? ResponseResult.success() : ResponseResult.error("邀请失败");
    }

    /**
     * 移除群组成员
     *
     * @param groupId 群组ID
     * @param userId  要移除的用户ID
     * @return 操作结果
     */
    @Operation(summary = "移除群组成员")
    @DeleteMapping("/{groupId}/member/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> removeGroupMember(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId,
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId) {

        boolean result = chatGroupService.removeGroupMember(groupId, userId, null); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("移除失败，可能没有权限");
    }

    /**
     * 退出群组
     *
     * @param groupId 群组ID
     * @return 操作结果
     */
    @Operation(summary = "退出群组")
    @PostMapping("/leave/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> leaveGroup(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId) {
        boolean result = chatGroupService.leaveGroup(groupId, null); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("退出失败");
    }

    /**
     * 获取群组成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    @Operation(summary = "获取群组成员列表")
    @GetMapping("/{groupId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<List<Map<String, Object>>> getGroupMembers(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId) {
        List<Map<String, Object>> members = chatGroupService.getGroupMembers(groupId);
        return ResponseResult.success(members);
    }

    /**
     * 更新群组成员角色
     *
     * @param groupId    群组ID
     * @param userId     用户ID
     * @param memberType 新的成员类型
     * @return 操作结果
     */
    @Operation(summary = "更新群组成员角色")
    @PutMapping("/{groupId}/member/{userId}/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> updateGroupMemberRole(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId,
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam @NotNull(message = "成员类型不能为空") Integer memberType) {

        boolean result = chatGroupService.updateGroupMemberRole(groupId, userId, memberType, null); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("更新失败，可能没有权限");
    }

    /**
     * 设置群组成员禁言状态
     *
     * @param groupId     群组ID
     * @param userId      用户ID
     * @param muteMinutes 禁言时长(分钟)，0表示解除禁言
     * @return 操作结果
     */
    @Operation(summary = "设置群组成员禁言状态")
    @PutMapping("/{groupId}/member/{userId}/mute")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> muteGroupMember(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId,
            @Parameter(description = "用户ID") @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam @NotNull(message = "禁言时长不能为空") Integer muteMinutes) {

        boolean result = chatGroupService.muteGroupMember(groupId, userId, muteMinutes, null); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("设置失败，可能没有权限");
    }

    /**
     * 申请加入群组
     *
     * @param groupId 群组ID
     * @param message 申请消息
     * @return 操作结果
     */
    @Operation(summary = "申请加入群组")
    @PostMapping("/{groupId}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> applyJoinGroup(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId,
            @RequestBody(required = false) String message) {

        boolean result = chatGroupService.applyJoinGroup(groupId, null, message); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("申请失败，可能已申请或已是成员");
    }

    /**
     * 处理入群申请
     *
     * @param applyId 申请ID
     * @param approve 是否批准
     * @param reason  拒绝原因(可选)
     * @return 操作结果
     */
    @Operation(summary = "处理入群申请")
    @PostMapping("/apply/{applyId}/handle")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> handleJoinGroupApply(
            @Parameter(description = "申请ID") @PathVariable @NotNull(message = "申请ID不能为空") Long applyId,
            @RequestParam @NotNull(message = "是否批准不能为空") Boolean approve,
            @RequestParam(required = false) String reason) {

        boolean result = chatGroupService.handleJoinGroupApply(applyId, approve, null, reason); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("处理失败，可能没有权限");
    }

    /**
     * 获取待处理的入群申请
     *
     * @param groupId 群组ID
     * @return 申请列表
     */
    @Operation(summary = "获取待处理的入群申请")
    @GetMapping("/{groupId}/pending-applications")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<List<Map<String, Object>>> getPendingGroupApplications(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId) {

        List<Map<String, Object>> applications = chatGroupService.getPendingGroupApplications(groupId, null); // null表示当前登录用户
        return ResponseResult.success(applications);
    }

    /**
     * 更新群组公告
     *
     * @param groupId      群组ID
     * @param announcement 公告内容
     * @return 操作结果
     */
    @Operation(summary = "更新群组公告")
    @PutMapping("/{groupId}/announcement")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> updateGroupAnnouncement(
            @Parameter(description = "群组ID") @PathVariable @NotNull(message = "群组ID不能为空") Long groupId,
            @RequestBody @NotNull(message = "公告内容不能为空") String announcement) {

        boolean result = chatGroupService.updateGroupAnnouncement(groupId, announcement, null); // null表示当前登录用户
        return result ? ResponseResult.success() : ResponseResult.error("更新失败，可能没有权限");
    }
}