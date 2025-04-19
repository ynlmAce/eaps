package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fq.yznu.eaps.entity.EnterpriseRating;

import java.util.List;
import java.util.Map;

/**
 * 企业评分服务接口
 */
public interface EnterpriseRatingService extends IService<EnterpriseRating> {

    /**
     * 提交企业评分
     * @param rating 评分信息
     * @return 是否成功
     */
    boolean submitRating(EnterpriseRating rating);

    /**
     * 更新企业评分
     * @param rating 评分信息
     * @return 是否成功
     */
    boolean updateRating(EnterpriseRating rating);

    /**
     * 删除企业评分
     * @param id 评分ID
     * @return 是否成功
     */
    boolean deleteRating(Long id);

    /**
     * 企业回复评分
     * @param id 评分ID
     * @param reply 回复内容
     * @return 是否成功
     */
    boolean replyRating(Long id, String reply);

    /**
     * 企业申诉评分
     * @param id 评分ID
     * @param appealContent 申诉内容
     * @return 是否成功
     */
    boolean appealRating(Long id, String appealContent);

    /**
     * 管理员审核评分
     * @param id 评分ID
     * @param status 审核状态：1-通过，2-不通过
     * @param reviewComment 审核意见
     * @return 是否成功
     */
    boolean reviewRating(Long id, Integer status, String reviewComment);

    /**
     * 管理员审核申诉
     * @param id 评分ID
     * @param appealStatus 申诉处理结果：1-驳回申诉，2-通过申诉
     * @param appealReviewComment 处理意见
     * @return 是否成功
     */
    boolean reviewAppeal(Long id, Integer appealStatus, String appealReviewComment);

    /**
     * 获取评分详情
     * @param id 评分ID
     * @return 评分详情
     */
    EnterpriseRating getRatingDetail(Long id);

    /**
     * 分页查询企业评分列表（前台展示）
     * @param enterpriseId 企业ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param orderBy 排序方式：latest-最新，highest-最高分，lowest-最低分，helpful-最有用
     * @return 评分列表
     */
    Page<Map<String, Object>> pageRatingList(Long enterpriseId, Integer pageNum, Integer pageSize, String orderBy);

    /**
     * 获取学生的评分列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 评分列表
     */
    Page<Map<String, Object>> getStudentRatingList(Integer pageNum, Integer pageSize);

    /**
     * 获取企业收到的评分列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 评分列表
     */
    Page<Map<String, Object>> getEnterpriseRatingList(Integer pageNum, Integer pageSize);

    /**
     * 管理员获取待处理的评分列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param type 查询类型：1-待审核评分，2-待处理申诉
     * @return 评分列表
     */
    Page<Map<String, Object>> getPendingRatingList(Integer pageNum, Integer pageSize, Integer type);

    /**
     * 获取企业平均评分
     * @param enterpriseId 企业ID
     * @return 平均评分信息，包含avgRating-平均分，totalCount-总评分数，distribution-各星级分布
     */
    Map<String, Object> getAverageRating(Long enterpriseId);

    /**
     * 标记评分为有用
     * @param id 评分ID
     * @return 是否成功
     */
    boolean markRatingHelpful(Long id);

    /**
     * 获取企业热门评分列表
     * @param enterpriseId 企业ID
     * @param limit 数量限制
     * @return 热门评分列表
     */
    List<Map<String, Object>> getHotRatingList(Long enterpriseId, Integer limit);
}