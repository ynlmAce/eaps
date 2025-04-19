package com.fq.yznu.eaps.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 就业统计视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID（学院ID/专业ID/班级ID）
     */
    private Long id;

    /**
     * 名称（学院名称/专业名称/班级名称）
     */
    private String name;

    /**
     * 总人数
     */
    private Integer totalCount;

    /**
     * 已就业人数
     */
    private Integer employedCount;

    /**
     * 就业率
     */
    private Double employmentRate;

    /**
     * 未就业人数
     */
    private Integer unemployedCount;

    /**
     * 考研人数
     */
    private Integer postgraduateCount;

    /**
     * 考研率
     */
    private Double postgraduateRate;

    /**
     * 创业人数
     */
    private Integer entrepreneurshipCount;

    /**
     * 创业率
     */
    private Double entrepreneurshipRate;

    /**
     * 其他去向人数
     */
    private Integer otherCount;
} 