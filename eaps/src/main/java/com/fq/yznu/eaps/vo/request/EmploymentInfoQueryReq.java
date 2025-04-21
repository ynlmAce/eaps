package com.fq.yznu.eaps.vo.request;

import lombok.Data;

/**
 * 就业信息查询请求
 */
@Data
public class EmploymentInfoQueryReq {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

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
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 就业状态
     */
    private Integer employmentStatus;

    /**
     * 审核状态
     */
    private Integer verifyStatus;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}