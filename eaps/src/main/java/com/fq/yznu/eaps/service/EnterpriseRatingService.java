package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fq.yznu.eaps.dto.EnterpriseRatingDTO;
import com.fq.yznu.eaps.entity.EnterpriseRating;
import com.fq.yznu.eaps.vo.RatingAppealReq;
import com.fq.yznu.eaps.vo.RatingReviewReq;

import java.util.List;
import java.util.Map;

/**
 * 企业评分服务接口
 */
public interface EnterpriseRatingService extends IService<EnterpriseRating> {

    /**
     * 提交企业评价
     * 
     * @param ratingDTO 评价信息
     */
    void submitRating(EnterpriseRatingDTO ratingDTO);

    /**
     * 更新评分
     *
     * @param rating 评分信息
     * @return 是否成功
     */
    boolean updateRating(EnterpriseRating rating);

    /**
     * 删除评分
     *
     * @param id 评分ID
     * @return 是否成功
     */
    boolean removeRating(Long id);

    /**
     * 删除评分
     *
     * @param id 评分ID
     * @return 是否成功
     */
    boolean deleteRating(Long id);

    /**
     * 回复评价
     *
     * @param id    评价ID
     * @param reply 回复内容
     * @return 是否成功
     */
    boolean replyRating(Long id, String reply);

    /**
     * 对评价进行申诉
     * 
     * @param appealReq 申诉请求
     * @return 是否成功
     */
    boolean appealRating(RatingAppealReq appealReq);

    /**
     * 审核评价
     *
     * @param reviewReq 审核请求
     * @return 是否成功
     */
    boolean reviewRating(RatingReviewReq reviewReq);

    /**
     * 审核评价
     *
     * @param id            评价ID
     * @param status        审核状态
     * @param reviewComment 审核意见
     * @return 是否成功
     */
    boolean reviewRating(Long id, Integer status, String reviewComment);

    /**
     * 获取评价详情
     * 
     * @param ratingId 评价ID
     * @return 评价详情
     */
    EnterpriseRating getRatingDetail(Long ratingId);

    /**
     * 分页查询评价列表
     *
     * @param params 查询参数
     * @return 评价列表
     */
    Page<Map<String, Object>> pageRatingList(Map<String, Object> params);

    /**
     * 获取评价列表
     *
     * @param enterpriseId 企业ID
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @return 评价列表
     */
    Page<Map<String, Object>> getRatingList(Long enterpriseId, Integer pageNum, Integer pageSize);

    /**
     * 获取学生评价列表
     *
     * @param studentId 学生ID
     * @param params    查询参数
     * @return 评价列表
     */
    Page<Map<String, Object>> getStudentRatingList(Long studentId, Map<String, Object> params);

    /**
     * 获取学生评价列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 评价列表
     */
    Page<Map<String, Object>> getStudentRatingList(Integer pageNum, Integer pageSize);

    /**
     * 获取企业评价列表
     *
     * @param enterpriseId 企业ID
     * @param params       查询参数
     * @return 评价列表
     */
    Page<Map<String, Object>> getEnterpriseRatingList(Long enterpriseId, Map<String, Object> params);

    /**
     * 获取企业评价列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 评价列表
     */
    Page<Map<String, Object>> getEnterpriseRatingList(Integer pageNum, Integer pageSize);

    /**
     * 获取待审核评价列表
     *
     * @param params 查询参数
     * @return 评价列表
     */
    Page<Map<String, Object>> getPendingRatingList(Map<String, Object> params);

    /**
     * 获取待审核评价列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param type     查询类型
     * @return 评价列表
     */
    Page<Map<String, Object>> getPendingRatingList(Integer pageNum, Integer pageSize, Integer type);

    /**
     * 获取企业平均评分
     *
     * @param enterpriseId 企业ID
     * @return 平均评分信息
     */
    Map<String, Object> getEnterpriseAverageRating(Long enterpriseId);

    /**
     * 获取热门评价列表
     *
     * @param enterpriseId 企业ID
     * @param limit        限制数量
     * @return 评价列表
     */
    List<Map<String, Object>> getHotRatingList(Long enterpriseId, Integer limit);

    /**
     * 标记评价有用
     *
     * @param id     评价ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markRatingHelpful(Long id, Long userId);

    /**
     * 统计待审核评价数量
     *
     * @return 待审核数量
     */
    Integer countPendingReview();

    /**
     * 审核申诉
     *
     * @param id                  评价ID
     * @param appealStatus        申诉处理结果
     * @param appealReviewComment 处理意见
     * @return 是否成功
     */
    boolean reviewAppeal(Long id, Integer appealStatus, String appealReviewComment);

    /**
     * 提交申诉
     *
     * @param id       评分ID
     * @param reason   申诉原因
     * @param evidence 申诉证据
     * @return 操作结果
     */
    boolean submitAppeal(Long id, String reason, String evidence);

    /**
     * 根据ID获取评分
     */
    EnterpriseRating getRatingById(Long id);
}