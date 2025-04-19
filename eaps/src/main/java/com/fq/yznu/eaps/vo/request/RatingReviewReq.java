package com.fq.yznu.eaps.vo.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 评分审核请求对象
 */
@Data
public class RatingReviewReq {

    /**
     * 评分ID
     */
    @NotNull(message = "评分ID不能为空")
    private Long ratingId;

    /**
     * 审核状态：1-通过，2-拒绝
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 审核意见
     */
    private String reviewRemark;
} 