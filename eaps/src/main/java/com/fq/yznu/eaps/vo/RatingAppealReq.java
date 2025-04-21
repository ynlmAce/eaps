package com.fq.yznu.eaps.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 评分申诉请求VO
 */
@Data
@Schema(description = "评分申诉请求")
public class RatingAppealReq {

    @NotNull(message = "评分ID不能为空")
    @Schema(description = "评分ID")
    private Long id;

    @NotBlank(message = "申诉内容不能为空")
    @Schema(description = "申诉内容")
    private String appealComment;

    @Schema(description = "申诉证据")
    private String appealEvidence;
}