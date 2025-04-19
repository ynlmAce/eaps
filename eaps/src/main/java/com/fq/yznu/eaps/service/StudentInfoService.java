package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.StudentInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 学生信息服务接口
 */
public interface StudentInfoService {

    /**
     * 获取当前学生信息
     *
     * @return 学生信息
     */
    Map<String, Object> getCurrentStudentInfo();

    /**
     * 更新学生信息
     *
     * @param studentInfo 学生信息
     * @param resume      简历文件（可选）
     */
    void updateStudentInfo(StudentInfo studentInfo, MultipartFile resume);

    /**
     * 获取学生列表
     *
     * @param params 查询参数
     * @return 学生列表与分页信息
     */
    Map<String, Object> getStudentList(Map<String, Object> params);

    /**
     * 获取学生详情
     *
     * @param id 学生ID
     * @return 学生详情
     */
    Map<String, Object> getStudentDetail(Long id);

    /**
     * 获取辅导员的学生列表
     *
     * @param params 查询参数
     * @return 学生列表与分页信息
     */
    Map<String, Object> getCounselorStudentList(Map<String, Object> params);

    /**
     * 更新学生就业状态
     *
     * @param id               学生ID
     * @param employmentStatus 就业状态（0：未就业，1：已就业）
     */
    void updateEmploymentStatus(Long id, Integer employmentStatus);

    /**
     * 获取就业统计信息
     *
     * @param params 查询参数
     * @return 就业统计信息
     */
    Map<String, Object> getEmploymentStatistics(Map<String, Object> params);
} 