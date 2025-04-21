package com.fq.yznu.eaps.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 企业宣传信息查询请求DTO
 */
@Data
@Schema(description = "推广查询请求")
public class PromotionQueryReq {

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    @Schema(description = "企业ID")
    private Long enterpriseId;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "推广类型")
    private Integer type;

    @Schema(description = "状态")
    private Integer status;
}