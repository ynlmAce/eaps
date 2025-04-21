package com.fq.yznu.eaps.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingReviewReq {
    @NotNull(message = "评分ID不能为空")
    private Long id;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @NotBlank(message = "审核意见不能为空")
    private String reviewComment;
}