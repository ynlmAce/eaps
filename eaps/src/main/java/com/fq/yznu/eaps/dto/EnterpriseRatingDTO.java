package com.fq.yznu.eaps.dto;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class EnterpriseRatingDTO {

    private Long id;

    @NotNull(message = "企业ID不能为空")
    private Long enterpriseId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private BigDecimal rating;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private String reply;

    private Integer appealStatus;

    private String appealReason;

    private String appealEvidence;

    private String reviewRemark;

    private Long createdBy;

    private String createdTime;

    private Long updatedBy;

    private String updatedTime;
}