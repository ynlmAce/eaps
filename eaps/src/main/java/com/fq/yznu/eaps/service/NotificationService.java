package com.fq.yznu.eaps.service;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送推广审核通知
     *
     * @param enterpriseId 企业ID
     * @param promotionId 推广ID
     * @param promotionTitle 推广标题
     * @param status 审核状态：1-通过、2-拒绝
     * @param reason 审核理由（拒绝时必填）
     */
    void sendPromotionReviewNotification(Long enterpriseId, Long promotionId, 
                                        String promotionTitle, Integer status, String reason);
    
    /**
     * 发送系统通知
     *
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     */
    void sendSystemNotification(Long userId, String title, String content, 
                              Long relatedId, String relatedType);
} 