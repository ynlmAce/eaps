package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.StudentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学生信息数据访问层
 */
@Mapper
public interface StudentInfoMapper extends BaseMapper<StudentInfo> {
    
    /**
     * 根据辅导员ID查询学生列表
     *
     * @param counselorId 辅导员ID
     * @return 学生列表
     */
    List<Map<String, Object>> selectStudentListByCounselorId(@Param("counselorId") Long counselorId);
    
    /**
     * 统计辅导员负责的学生就业情况
     *
     * @param counselorId 辅导员ID
     * @return 就业统计信息
     */
    Map<String, Object> countEmploymentStatusByCounselorId(@Param("counselorId") Long counselorId);
} 