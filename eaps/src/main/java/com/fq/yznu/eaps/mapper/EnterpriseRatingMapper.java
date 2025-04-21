package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.EnterpriseRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 企业评分数据访问层
 */
@Mapper
public interface EnterpriseRatingMapper extends BaseMapper<EnterpriseRating> {
    
    /**
     * 分页查询评价列表
     *
     * @param page          分页参数
     * @param enterpriseId  企业ID
     * @param studentId     学生ID
     * @param status        状态
     * @param keyword       关键词
     * @return 评价列表
     */
    Page<Map<String, Object>> selectRatingPage(
            Page<Map<String, Object>> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("studentId") Long studentId,
            @Param("status") Integer status,
            @Param("keyword") String keyword);
    
    /**
     * 获取企业平均评分
     *
     * @param enterpriseId 企业ID
     * @return 评分信息
     */
    Map<String, Object> selectEnterpriseAverageRating(@Param("enterpriseId") Long enterpriseId);
    
    /**
     * 获取热门评价列表
     *
     * @param enterpriseId 企业ID
     * @param limit        限制数量
     * @return 评价列表
     */
    List<Map<String, Object>> selectHotRatingList(
            @Param("enterpriseId") Long enterpriseId,
            @Param("limit") Integer limit);
    
    /**
     * 增加有用计数
     *
     * @param id 评价ID
     * @return 影响行数
     */
    @Update("UPDATE enterprise_rating SET helpful_count = helpful_count + 1 WHERE id = #{id}")
    int incrementHelpfulCount(@Param("id") Long id);
    
    /**
     * 统计待审核评价数量
     *
     * @return 待审核数量
     */
    Integer countPendingReview();
} 