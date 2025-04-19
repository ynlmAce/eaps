package com.fq.yznu.eaps.vo.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 企业宣传信息审核请求DTO
 */
@Data
public class PromotionReviewReq {

    /**
     * 宣传信息ID
     */
    @NotNull(message = "宣传信息ID不能为空")
    private Long id;

    /**
     * 审核状态：1已通过，2已拒绝
     */
    @NotNull(message = "审核状态不能为空")
    @Min(value = 1, message = "审核状态不正确")
    @Max(value = 2, message = "审核状态不正确")
    private Integer status;

    /**
     * 审核备注
     */
    private String reviewRemark;
} 