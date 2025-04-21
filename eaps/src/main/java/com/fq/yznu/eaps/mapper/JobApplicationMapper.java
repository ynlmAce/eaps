package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 职位申请数据访问层
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 获取学生的申请统计
     *
     * @param studentId 学生ID
     * @return 统计信息
     */
    Map<String, Object> selectStudentApplicationStatistics(@Param("studentId") Long studentId);

    /**
     * 获取企业的申请统计
     *
     * @param enterpriseId 企业ID
     * @return 统计信息
     */
    Map<String, Object> selectEnterpriseApplicationStatistics(@Param("enterpriseId") Long enterpriseId);

    /**
     * 获取职位的申请统计
     *
     * @param jobId 职位ID
     * @return 统计信息
     */
    Map<String, Object> selectJobApplicationStatistics(@Param("jobId") Long jobId);
}