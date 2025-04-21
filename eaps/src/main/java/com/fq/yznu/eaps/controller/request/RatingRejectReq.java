package com.fq.yznu.eaps.controller.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 评价拒绝请求
 */
@Data
public class RatingRejectReq {

    /**
     * 拒绝理由
     */
    @NotBlank(message = "拒绝理由不能为空")
    private String rejectReason;

    /**
     * 详细说明
     */
    private String rejectComment;
}