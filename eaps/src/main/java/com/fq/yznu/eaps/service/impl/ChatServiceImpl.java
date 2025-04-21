package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.entity.ChatConversation;
import com.fq.yznu.eaps.entity.ChatMessage;
import com.fq.yznu.eaps.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        // 实现发送消息的逻辑
        return null;
    }

    @Override
    public Page<ChatMessage> getMessageHistory(Long conversationId, Integer pageNum, Integer pageSize) {
        // 实现获取消息历史的逻辑
        return null;
    }

    @Override
    public List<Map<String, Object>> getConversationList(Long userId) {
        // 实现获取会话列表的逻辑
        return null;
    }

    @Override
    public ChatConversation getOrCreateConversation(Long userId, Long targetId, Integer conversationType, Long jobId) {
        // 实现创建或获取会话的逻辑
        return null;
    }

    @Override
    public boolean markMessagesAsRead(Long conversationId, Long userId) {
        // 实现标记消息为已读的逻辑
        return false;
    }

    @Override
    public boolean recallMessage(Long messageId, Long userId) {
        // 实现撤回消息的逻辑
        return false;
    }

    @Override
    public boolean deleteConversation(Long conversationId, Long userId) {
        // 实现删除会话的逻辑
        return false;
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        // 实现获取未读消息数的逻辑
        return 0;
    }

    @Override
    public ChatMessage uploadAndSendFileMessage(ChatMessage message, MultipartFile file) {
        log.info("处理文件上传和消息发送: 文件名={}, 大小={}, 消息类型={}",
                file.getOriginalFilename(), file.getSize(), message.getMessageType());

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException("文件名不能为空");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String subDir = "chat/files";
            String filePath = uploadPath + File.separator + subDir + File.separator + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + File.separator + subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            file.transferTo(new File(filePath));
            message.setContent(filePath);

            // 调用发送消息方法
            return sendMessage(message);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
}