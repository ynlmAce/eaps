package com.fq.yznu.eaps.vo.request;

import lombok.Data;

/**
 * 就业信息查询请求DTO
 */
@Data
public class EmploymentInfoQueryReq {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 就业状态：0未就业，1已就业，2升学，3创业，4灵活就业
     */
    private Integer employmentStatus;

    /**
     * 审核状态：0待审核，1已通过，2已拒绝
     */
    private Integer verifyStatus;

    /**
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 学院ID
     */
    private Long collegeId;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 关键词搜索（学生姓名、学号、单位名称、岗位名称）
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