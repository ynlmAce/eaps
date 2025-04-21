package com.fq.yznu.eaps.controller.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 评价申诉请求
 */
@Data
public class RatingAppealReq {

    /**
     * 评价ID
     */
    @NotNull(message = "评价ID不能为空")
    private Long ratingId;

    /**
     * 申诉理由
     */
    @NotBlank(message = "申诉理由不能为空")
    private String appealReason;

    /**
     * 申诉证据描述
     */
    @NotBlank(message = "申诉证据描述不能为空")
    private String appealEvidence;

    /**
     * 证据图片URL列表
     */
    private List<String> evidenceUrls;
} 