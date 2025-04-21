package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.JobPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 职位数据访问层
 */
@Mapper
public interface JobPositionMapper extends BaseMapper<JobPosition> {
    
    /**
     * 获取职位详情，包含企业信息
     *
     * @param id 职位ID
     * @return 职位详情
     */
    Map<String, Object> selectPositionDetail(@Param("id") Long id);
    
    /**
     * 分页查询职位列表，包含企业信息
     *
     * @param page         分页参数
     * @param enterpriseId 企业ID
     * @param keyword      关键词
     * @param city         城市
     * @param salary       薪资范围
     * @param education    学历要求
     * @param experience   经验要求
     * @param status       状态
     * @param orderBy      排序方式
     * @return 职位列表
     */
    Page<Map<String, Object>> selectPositionPage(
            Page<Map<String, Object>> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("salary") Integer salary,
            @Param("education") Integer education,
            @Param("experience") Integer experience,
            @Param("status") Integer status,
            @Param("orderBy") String orderBy);
    
    /**
     * 获取热门职位列表
     *
     * @param limit 限制数量
     * @return 职位列表
     */
    List<Map<String, Object>> selectHotPositionList(@Param("limit") Integer limit);
    
    /**
     * 获取最新职位列表
     *
     * @param limit 限制数量
     * @return 职位列表
     */
    List<Map<String, Object>> selectLatestPositionList(@Param("limit") Integer limit);
    
    /**
     * 增加浏览次数
     *
     * @param id 职位ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);
    
    /**
     * 统计待审核职位数量
     *
     * @return 待审核数量
     */
    Integer countPendingReview();
} 