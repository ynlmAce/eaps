package com.fq.yznu.eaps.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 企业宣传信息审核请求DTO
 */
@Data
@Schema(description = "推广审核请求")
public class PromotionReviewReq {

    @Schema(description = "推广ID", hidden = true)
    private Long id;

    /**
     * 审核状态：1已通过，2已拒绝
     */
    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1通过，2拒绝")
    @Min(value = 1, message = "审核状态不正确")
    @Max(value = 2, message = "审核状态不正确")
    private Integer status;

    /**
     * 审核备注
     */
    @Schema(description = "审核备注")
    private String reviewRemark;
}