package com.fq.yznu.eaps.vo.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 评分申诉请求对象
 */
@Data
public class RatingAppealReq {

    /**
     * 评分ID
     */
    @NotNull(message = "评分ID不能为空")
    private Long ratingId;

    /**
     * 申诉原因
     */
    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;

    /**
     * 证据描述
     */
    private String evidenceDescription;

    /**
     * 证据文件URL，多个以逗号分隔
     */
    private String evidenceUrls;
} 