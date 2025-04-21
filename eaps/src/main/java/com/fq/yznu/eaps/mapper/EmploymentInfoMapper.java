package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.EmploymentInfo;
import com.fq.yznu.eaps.vo.EmploymentStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 就业信息Mapper接口
 */
@Mapper
public interface EmploymentInfoMapper extends BaseMapper<EmploymentInfo> {

    /**
     * 分页查询就业信息列表
     *
     * @param page         分页参数
     * @param studentId    学生ID
     * @param enterpriseId 企业ID
     * @param status       状态
     * @return 就业信息分页数据
     */
    IPage<EmploymentInfo> selectEmploymentInfoPage(Page<EmploymentInfo> page,
            @Param("studentId") Long studentId,
            @Param("enterpriseId") Long enterpriseId,
            @Param("status") Integer status);

    /**
     * 查询就业统计数据
     *
     * @param collegeId      学院ID
     * @param majorId        专业ID
     * @param classId        班级ID
     * @param graduationYear 毕业年份
     * @return 就业统计数据
     */
    List<EmploymentStatisticsVO> selectEmploymentStatistics(@Param("collegeId") Long collegeId,
            @Param("majorId") Long majorId,
            @Param("classId") Long classId,
            @Param("graduationYear") Integer graduationYear);

    /**
     * 按学院统计就业情况
     * 
     * @param graduationYear 毕业年份
     * @return 各学院就业统计数据
     */
    List<Map<String, Object>> countEmploymentByCollege(@Param("graduationYear") Integer graduationYear);

    /**
     * 按专业统计就业情况
     * 
     * @param graduationYear 毕业年份
     * @param collegeId      学院ID
     * @return 专业就业统计数据
     */
    List<Map<String, Object>> countEmploymentByMajor(@Param("graduationYear") Integer graduationYear,
            @Param("collegeId") Long collegeId);

    /**
     * 按班级统计就业情况
     * 
     * @param graduationYear 毕业年份
     * @param majorId        专业ID
     * @return 班级就业统计数据
     */
    List<Map<String, Object>> countEmploymentByClass(@Param("graduationYear") Integer graduationYear,
            @Param("majorId") Long majorId);
}