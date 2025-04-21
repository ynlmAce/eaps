package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fq.yznu.eaps.entity.EnterprisePromotion;
import com.fq.yznu.eaps.vo.request.PromotionReviewReq;
import com.fq.yznu.eaps.vo.response.PromotionDetailVO;

import java.util.List;
import java.util.Map;

/**
 * 企业宣传信息服务接口
 */
public interface EnterprisePromotionService extends IService<EnterprisePromotion> {

    /**
     * 分页查询企业宣传信息列表（接收Map参数）
     *
     * @param params 包含分页和查询参数的Map
     * @return 分页结果和数据列表
     */
    Map<String, Object> pageList(Map<String, Object> params);

    /**
     * 分页查询企业宣传信息列表
     *
     * @param page         分页参数
     * @param enterpriseId 企业ID
     * @param title        标题关键词
     * @param type         宣传类型
     * @param status       状态
     * @return 分页结果
     */
    IPage<EnterprisePromotion> pageList(Page<EnterprisePromotion> page, Long enterpriseId, String title,
            Integer type, Integer status);

    /**
     * 获取企业宣传信息详情
     *
     * @param id 宣传信息ID
     * @return 宣传信息详情
     */
    EnterprisePromotion getPromotionDetail(Long id);

    /**
     * 保存企业宣传信息
     *
     * @param promotion 宣传信息
     * @return 是否成功
     */
    boolean savePromotion(EnterprisePromotion promotion);

    /**
     * 更新企业宣传信息
     *
     * @param promotion 宣传信息
     * @return 是否成功
     */
    boolean updatePromotion(EnterprisePromotion promotion);

    /**
     * 审核企业宣传信息
     *
     * @param reviewReq 审核请求
     * @return 是否成功
     */
    boolean reviewPromotion(PromotionReviewReq reviewReq);

    /**
     * 删除企业宣传信息
     *
     * @param id 宣传信息ID
     * @return 是否成功
     */
    boolean removePromotion(Long id);

    /**
     * 批量删除企业宣传信息
     *
     * @param ids 宣传信息ID列表
     * @return 是否成功
     */
    boolean batchRemovePromotion(List<Long> ids);

    /**
     * 设置置顶状态
     *
     * @param id    宣传信息ID
     * @param isTop 是否置顶：0否，1是
     * @return 是否成功
     */
    boolean setTopStatus(Long id, Integer isTop);

    /**
     * 增加浏览量
     *
     * @param id 宣传信息ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long id);

    /**
     * 查询待审核的宣传信息数量
     *
     * @return 待审核数量
     */
    Integer countPendingReview();

    /**
     * 获取最新的企业宣传列表
     *
     * @param type  宣传类型：1企业介绍，2宣讲会通知
     * @param limit 限制数量
     * @return 宣传信息列表
     */
    List<EnterprisePromotion> getLatestPromotions(Integer type, Integer limit);

    /**
     * 获取热门企业宣传列表
     *
     * @param type  宣传类型：1企业介绍，2宣讲会通知
     * @param limit 限制数量
     * @return 宣传信息列表
     */
    List<EnterprisePromotion> getHotPromotions(Integer type, Integer limit);

    /**
     * 审核宣传信息
     *
     * @param id             宣传信息ID
     * @param status         审核状态：1-通过、2-拒绝
     * @param reviewComments 审核意见
     * @param reviewerId     审核人ID
     * @return 是否成功
     */
    boolean review(Long id, Integer status, String reviewComments, Long reviewerId);

    /**
     * 获取特定类型的最新宣传信息列表
     *
     * @param type  宣传类型
     * @param limit 限制数量
     * @return 宣传信息列表
     */
    IPage<EnterprisePromotion> getLatestByType(Integer type, Integer limit);

    /**
     * 获取企业推广详情（包含企业信息）
     *
     * @param id 推广ID
     * @return 推广详情视图对象
     */
    PromotionDetailVO getPromotionDetailWithEnterpriseInfo(Long id);
}