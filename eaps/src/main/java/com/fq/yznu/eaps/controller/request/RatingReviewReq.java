package com.fq.yznu.eaps.controller.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 评价审核请求
 */
@Data
public class RatingReviewReq {

    /**
     * 评价ID
     */
    @NotNull(message = "评价ID不能为空")
    private Long ratingId;

    /**
     * 审核状态（1：通过，2：拒绝）
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    /**
     * 审核备注
     */
    private String reviewRemark;
}