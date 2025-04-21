package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.entity.Notification;
import com.fq.yznu.eaps.mapper.NotificationMapper;
import com.fq.yznu.eaps.service.EnterpriseInfoService;
import com.fq.yznu.eaps.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPromotionReviewNotification(Long enterpriseId, Long promotionId,
            String promotionTitle, Integer status, String reason) {
        if (enterpriseId == null || promotionId == null) {
            log.warn("发送推广审核通知失败，参数不完整：enterpriseId={}, promotionId={}", enterpriseId, promotionId);
            return;
        }

        // 获取企业信息，找到对应的用户ID
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.getEnterpriseInfo(enterpriseId);
        if (enterpriseInfo == null || enterpriseInfo.getUserId() == null) {
            log.warn("发送推广审核通知失败，未找到企业信息：enterpriseId={}", enterpriseId);
            return;
        }

        Long userId = enterpriseInfo.getUserId();

        // 构建通知标题和内容
        String title = "推广内容审核结果通知";

        String content;
        if (status == 1) {
            // 审核通过
            content = String.format("您的推广内容“%s”已审核通过，现已在系统中公开展示。",
                    StringUtils.hasText(promotionTitle) ? promotionTitle : "未命名推广");
        } else if (status == 2) {
            // 审核拒绝
            content = String.format("您的推广内容“%s”未通过审核。%s",
                    StringUtils.hasText(promotionTitle) ? promotionTitle : "未命名推广",
                    StringUtils.hasText(reason) ? "原因：" + reason : "");
        } else {
            log.warn("发送推广审核通知失败，状态码无效：status={}", status);
            return;
        }

        // 调用系统通知方法
        sendSystemNotification(userId, title, content, promotionId, "promotion");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotification(Long userId, String title, String content,
            Long relatedId, String relatedType) {
        if (userId == null) {
            log.warn("发送系统通知失败，用户ID为空");
            return;
        }

        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(2); // 审核通知
            notification.setRelatedId(relatedId);
            notification.setRelatedType(relatedType);
            notification.setIsRead(0); // 未读
            notification.setStatus(1); // 已发送
            notification.setDeleted(0); // 未删除

            LocalDateTime now = LocalDateTime.now();
            notification.setCreateTime(now);
            notification.setSendTime(now);

            this.save(notification);
            log.info("成功发送通知：userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("发送通知失败：userId={}, title={}", userId, title, e);
            throw e;
        }
    }
}