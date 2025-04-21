package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.ChatConversation;
import com.fq.yznu.eaps.entity.ChatMessage;
import com.fq.yznu.eaps.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@Tag(name = "聊天功能接口")
@RestController
@RequestMapping("/api/chat")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送消息
     *
     * @param message 消息内容
     * @return 发送结果
     */
    @Operation(summary = "发送消息")
    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<ChatMessage> sendMessage(@RequestBody @Valid ChatMessage message) {
        ChatMessage result = chatService.sendMessage(message);
        return ResponseResult.success(result);
    }

    /**
     * 发送图片或文件消息
     *
     * @param conversationId 会话ID
     * @param receiverId     接收者ID
     * @param messageType    消息类型（1-图片，2-文件）
     * @param file           文件
     * @return 发送结果
     */
    @Operation(summary = "发送图片或文件消息")
    @PostMapping("/send/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<ChatMessage> sendFileMessage(
            @RequestParam @NotNull(message = "会话ID不能为空") Long conversationId,
            @RequestParam @NotNull(message = "接收者ID不能为空") Long receiverId,
            @RequestParam @NotNull(message = "消息类型不能为空") Integer messageType,
            @RequestParam("file") @NotNull(message = "文件不能为空") MultipartFile file,
            @RequestParam(required = false) Long jobId) {

        // 构造消息对象
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setReceiverId(receiverId);
        message.setMessageType(messageType);
        message.setJobId(jobId);

        // 交由service处理文件上传和消息发送
        ChatMessage result = chatService.uploadAndSendFileMessage(message, file);
        return ResponseResult.success(result);
    }

    /**
     * 获取聊天历史记录
     *
     * @param conversationId 会话ID
     * @param pageNum        页码
     * @param pageSize       每页大小
     * @return 消息列表
     */
    @Operation(summary = "获取聊天历史记录")
    @GetMapping("/history/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Page<ChatMessage>> getMessageHistory(
            @PathVariable @NotNull(message = "会话ID不能为空") Long conversationId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页条数最小为1") @Max(value = 50, message = "每页条数最大为50") Integer pageSize) {

        Page<ChatMessage> page = chatService.getMessageHistory(conversationId, pageNum, pageSize);
        return ResponseResult.success(page);
    }

    /**
     * 获取会话列表
     *
     * @return 会话列表
     */
    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<List<Map<String, Object>>> getConversationList() {
        List<Map<String, Object>> list = chatService.getConversationList(null); // null表示当前登录用户
        return ResponseResult.success(list);
    }

    /**
     * 创建或获取与指定用户的会话
     *
     * @param targetId         目标用户ID
     * @param conversationType 会话类型（0-私聊，1-群聊）
     * @param jobId            相关职位ID（可选）
     * @return 会话信息
     */
    @Operation(summary = "创建或获取会话")
    @PostMapping("/conversation")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<ChatConversation> getOrCreateConversation(
            @RequestParam @NotNull(message = "目标用户ID不能为空") Long targetId,
            @RequestParam(defaultValue = "0") Integer conversationType,
            @RequestParam(required = false) Long jobId) {

        ChatConversation conversation = chatService.getOrCreateConversation(null, targetId, conversationType, jobId);
        return ResponseResult.success(conversation);
    }

    /**
     * 标记消息为已读
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "标记消息为已读")
    @PutMapping("/read/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> markMessagesAsRead(@PathVariable @NotNull(message = "会话ID不能为空") Long conversationId) {
        boolean result = chatService.markMessagesAsRead(conversationId, null);
        return result ? ResponseResult.success() : ResponseResult.error("标记失败");
    }

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    @Operation(summary = "撤回消息")
    @PutMapping("/recall/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> recallMessage(@PathVariable @NotNull(message = "消息ID不能为空") Long messageId) {
        boolean result = chatService.recallMessage(messageId, null);
        return result ? ResponseResult.success() : ResponseResult.error("撤回失败，可能超过撤回时间限制");
    }

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> deleteConversation(@PathVariable @NotNull(message = "会话ID不能为空") Long conversationId) {
        boolean result = chatService.deleteConversation(conversationId, null);
        return result ? ResponseResult.success() : ResponseResult.error("删除失败");
    }

    /**
     * 获取未读消息数
     *
     * @return 未读消息数
     */
    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Integer> getUnreadMessageCount() {
        int count = chatService.getUnreadMessageCount(null);
        return ResponseResult.success(count);
    }
}