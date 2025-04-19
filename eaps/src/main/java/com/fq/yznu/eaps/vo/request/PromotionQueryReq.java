package com.fq.yznu.eaps.vo.request;

import lombok.Data;

/**
 * 企业宣传信息查询请求DTO
 */
@Data
public class PromotionQueryReq {

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 宣传类型：1企业介绍，2宣讲会通知
     */
    private Integer type;

    /**
     * 审核状态：0待审核，1已通过，2已拒绝
     */
    private Integer status;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
} 