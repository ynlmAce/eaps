package com.fq.yznu.eaps.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 就业信息导出VO
 */
@Data
@HeadRowHeight(20)
public class EmploymentExportVO {

    /**
     * 学号
     */
    @ExcelProperty("学号")
    @ColumnWidth(15)
    private String studentId;

    /**
     * 姓名
     */
    @ExcelProperty("姓名")
    @ColumnWidth(10)
    private String studentName;

    /**
     * 性别
     */
    @ExcelProperty("性别")
    @ColumnWidth(5)
    private String gender;

    /**
     * 学院
     */
    @ExcelProperty("学院")
    @ColumnWidth(20)
    private String college;

    /**
     * 专业
     */
    @ExcelProperty("专业")
    @ColumnWidth(20)
    private String major;

    /**
     * 班级
     */
    @ExcelProperty("班级")
    @ColumnWidth(15)
    private String className;

    /**
     * 毕业年份
     */
    @ExcelProperty("毕业年份")
    @ColumnWidth(10)
    private Integer graduationYear;

    /**
     * 就业状态
     */
    @ExcelProperty("就业状态")
    @ColumnWidth(10)
    private String employmentStatus;

    /**
     * 就业类型
     */
    @ExcelProperty("就业类型")
    @ColumnWidth(10)
    private String employmentType;

    /**
     * 单位名称
     */
    @ExcelProperty("单位名称")
    @ColumnWidth(25)
    private String companyName;

    /**
     * 单位性质
     */
    @ExcelProperty("单位性质")
    @ColumnWidth(10)
    private String companyType;

    /**
     * 工作城市
     */
    @ExcelProperty("工作城市")
    @ColumnWidth(15)
    private String workCity;

    /**
     * 工作省份
     */
    @ExcelProperty("工作省份")
    @ColumnWidth(10)
    private String workProvince;

    /**
     * 岗位名称
     */
    @ExcelProperty("岗位名称")
    @ColumnWidth(20)
    private String positionName;

    /**
     * 月薪（元）
     */
    @ExcelProperty("月薪(元)")
    @ColumnWidth(10)
    private Integer monthlySalary;

    /**
     * 合同期限（月）
     */
    @ExcelProperty("合同期限(月)")
    @ColumnWidth(12)
    private Integer contractDuration;

    /**
     * 升学院校
     */
    @ExcelProperty("升学院校")
    @ColumnWidth(25)
    private String furtherSchool;

    /**
     * 升学专业
     */
    @ExcelProperty("升学专业")
    @ColumnWidth(20)
    private String furtherMajor;

    /**
     * 学历层次
     */
    @ExcelProperty("学历层次")
    @ColumnWidth(10)
    private String educationLevel;

    /**
     * 创业项目名称
     */
    @ExcelProperty("创业项目名称")
    @ColumnWidth(20)
    private String entrepreneurshipProject;

    /**
     * 联系电话
     */
    @ExcelProperty("联系电话")
    @ColumnWidth(15)
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @ExcelProperty("联系邮箱")
    @ColumnWidth(20)
    private String contactEmail;

    /**
     * 审核状态
     */
    @ExcelProperty("审核状态")
    @ColumnWidth(10)
    private String verifyStatus;
} 