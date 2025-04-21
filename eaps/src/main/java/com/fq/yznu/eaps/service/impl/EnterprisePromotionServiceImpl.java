package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.entity.EnterprisePromotion;
import com.fq.yznu.eaps.mapper.EnterprisePromotionMapper;
import com.fq.yznu.eaps.service.EnterpriseInfoService;
import com.fq.yznu.eaps.service.EnterprisePromotionService;
import com.fq.yznu.eaps.service.NotificationService;
import com.fq.yznu.eaps.vo.request.PromotionReviewReq;
import com.fq.yznu.eaps.vo.response.PromotionDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业宣传信息服务实现类
 */
@Slf4j
@Service
public class EnterprisePromotionServiceImpl extends ServiceImpl<EnterprisePromotionMapper, EnterprisePromotion>
        implements EnterprisePromotionService {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Map<String, Object> pageList(Map<String, Object> params) {
        // 从参数中提取分页信息
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "10").toString());

        // 从参数中提取查询条件
        Long enterpriseId = null;
        if (params.containsKey("enterpriseId") && params.get("enterpriseId") != null) {
            enterpriseId = Long.valueOf(params.get("enterpriseId").toString());
        }

        String keyword = (String) params.getOrDefault("keyword", "");

        Integer type = null;
        if (params.containsKey("type") && params.get("type") != null) {
            type = Integer.valueOf(params.get("type").toString());
        }

        Integer status = null;
        if (params.containsKey("status") && params.get("status") != null) {
            status = Integer.valueOf(params.get("status").toString());
        }

        // 创建分页对象
        Page<EnterprisePromotion> page = new Page<>(pageNum, pageSize);

        // 调用分页查询方法
        IPage<EnterprisePromotion> pageResult = pageList(page, enterpriseId, keyword, type, status);

        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", pageResult.getRecords());
        result.put("pageNum", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        result.put("pages", pageResult.getPages());

        return result;
    }

    @Override
    public IPage<EnterprisePromotion> pageList(Page<EnterprisePromotion> page, Long enterpriseId, String title,
            Integer type, Integer status) {
        LambdaQueryWrapper<EnterprisePromotion> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (enterpriseId != null) {
            queryWrapper.eq(EnterprisePromotion::getEnterpriseId, enterpriseId);
        }

        if (StringUtils.hasText(title)) {
            queryWrapper.like(EnterprisePromotion::getTitle, title);
        }

        if (type != null) {
            queryWrapper.eq(EnterprisePromotion::getType, type);
        }

        if (status != null) {
            queryWrapper.eq(EnterprisePromotion::getStatus, status);
        } else {
            // 默认只查询已审核通过的
            queryWrapper.eq(EnterprisePromotion::getStatus, 1);
        }

        // 未删除的记录
        queryWrapper.eq(EnterprisePromotion::getDeleted, 0);

        // 按置顶状态和创建时间排序
        queryWrapper.orderByDesc(EnterprisePromotion::getIsTop)
                .orderByDesc(EnterprisePromotion::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public EnterprisePromotion getPromotionDetail(Long id) {
        if (id == null) {
            log.warn("宣传信息ID为空");
            return null;
        }

        EnterprisePromotion promotion = this.getById(id);
        if (promotion == null || promotion.getDeleted() == 1) {
            log.warn("宣传信息不存在或已删除, id: {}", id);
            return null;
        }

        return promotion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePromotion(EnterprisePromotion promotion) {
        if (promotion == null) {
            log.warn("宣传信息为空");
            return false;
        }

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        promotion.setCreateTime(now);
        promotion.setUpdateTime(now);

        // 设置初始值
        promotion.setViewCount(0);
        promotion.setStatus(0); // 默认待审核
        promotion.setDeleted(0); // 未删除
        promotion.setIsTop(0); // 默认不置顶

        return this.save(promotion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePromotion(EnterprisePromotion promotion) {
        if (promotion == null || promotion.getId() == null) {
            log.warn("宣传信息为空或ID为空");
            return false;
        }

        // 查询原记录
        EnterprisePromotion existingPromotion = this.getById(promotion.getId());
        if (existingPromotion == null || existingPromotion.getDeleted() == 1) {
            log.warn("宣传信息不存在或已删除, id: {}", promotion.getId());
            return false;
        }

        // 企业只能修改自己发布的宣传信息
        if (!existingPromotion.getEnterpriseId().equals(promotion.getEnterpriseId())) {
            log.warn("无权修改该宣传信息, id: {}, enterpriseId: {}", promotion.getId(), promotion.getEnterpriseId());
            return false;
        }

        // 更新时间
        promotion.setUpdateTime(LocalDateTime.now());

        // 修改后状态设为待审核
        promotion.setStatus(0);

        // 保留原有字段
        promotion.setCreateTime(existingPromotion.getCreateTime());
        promotion.setViewCount(existingPromotion.getViewCount());
        promotion.setDeleted(existingPromotion.getDeleted());

        return this.updateById(promotion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewPromotion(PromotionReviewReq reviewReq) {
        if (reviewReq == null || reviewReq.getId() == null) {
            log.warn("审核请求为空或宣传信息ID为空");
            return false;
        }

        return review(reviewReq.getId(), reviewReq.getStatus(), reviewReq.getReviewRemark(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementViewCount(Long id) {
        EnterprisePromotion promotion = this.getById(id);
        if (promotion == null) {
            log.warn("宣传信息不存在, id: {}", id);
            return false;
        }

        // 更新浏览量，使用SQL直接+1，避免并发问题
        return this.baseMapper.incrementViewCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePromotion(Long id) {
        if (id == null) {
            log.warn("宣传信息ID为空");
            return false;
        }

        EnterprisePromotion promotion = this.getById(id);
        if (promotion == null) {
            log.warn("宣传信息不存在, id: {}", id);
            return false;
        }

        // 逻辑删除
        promotion.setDeleted(1);
        promotion.setUpdateTime(LocalDateTime.now());

        return this.updateById(promotion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemovePromotion(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("宣传信息ID列表为空");
            return false;
        }

        // 逻辑删除，批量更新
        return this.lambdaUpdate()
                .set(EnterprisePromotion::getDeleted, 1)
                .set(EnterprisePromotion::getUpdateTime, LocalDateTime.now())
                .in(EnterprisePromotion::getId, ids)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean review(Long id, Integer status, String reviewComments, Long reviewerId) {
        EnterprisePromotion promotion = this.getById(id);
        if (promotion == null) {
            log.warn("宣传信息不存在, id: {}", id);
            return false;
        }

        // 只有待审核状态的宣传信息才能进行审核
        if (promotion.getStatus() != 0) {
            log.warn("宣传信息当前状态无法审核, id: {}, status: {}", id, promotion.getStatus());
            return false;
        }

        promotion.setStatus(status);
        promotion.setReviewComments(reviewComments);
        promotion.setReviewerId(reviewerId);
        promotion.setReviewTime(LocalDateTime.now());
        promotion.setUpdateTime(LocalDateTime.now());

        // 更新推广信息
        boolean result = this.updateById(promotion);

        // 如果更新成功，发送审核通知
        if (result) {
            try {
                notificationService.sendPromotionReviewNotification(
                        promotion.getEnterpriseId(),
                        promotion.getId(),
                        promotion.getTitle(),
                        status,
                        reviewComments);
            } catch (Exception e) {
                // 记录日志但不影响事务提交
                log.error("发送推广审核通知失败", e);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setTopStatus(Long id, Integer isTop) {
        EnterprisePromotion promotion = this.getById(id);
        if (promotion == null) {
            log.warn("宣传信息不存在, id: {}", id);
            return false;
        }

        promotion.setIsTop(isTop);
        promotion.setUpdateTime(LocalDateTime.now());

        return this.updateById(promotion);
    }

    @Override
    public IPage<EnterprisePromotion> getLatestByType(Integer type, Integer limit) {
        Page<EnterprisePromotion> page = new Page<>(1, limit);
        LambdaQueryWrapper<EnterprisePromotion> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(EnterprisePromotion::getStatus, 1)
                .eq(EnterprisePromotion::getDeleted, 0);

        if (type != null) {
            queryWrapper.eq(EnterprisePromotion::getType, type);
        }

        queryWrapper.orderByDesc(EnterprisePromotion::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public List<EnterprisePromotion> getLatestPromotions(Integer type, Integer limit) {
        IPage<EnterprisePromotion> pageResult = getLatestByType(type, limit);
        return pageResult.getRecords();
    }

    @Override
    public List<EnterprisePromotion> getHotPromotions(Integer type, Integer limit) {
        LambdaQueryWrapper<EnterprisePromotion> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(EnterprisePromotion::getStatus, 1)
                .eq(EnterprisePromotion::getDeleted, 0);

        if (type != null) {
            queryWrapper.eq(EnterprisePromotion::getType, type);
        }

        // 按浏览量排序获取热门推广
        queryWrapper.orderByDesc(EnterprisePromotion::getViewCount);

        Page<EnterprisePromotion> page = new Page<>(1, limit);
        IPage<EnterprisePromotion> pageResult = this.page(page, queryWrapper);

        return pageResult.getRecords();
    }

    @Override
    public Integer countPendingReview() {
        return this.baseMapper.countPendingReview();
    }

    @Override
    public PromotionDetailVO getPromotionDetailWithEnterpriseInfo(Long id) {
        // 获取推广信息
        EnterprisePromotion promotion = this.getPromotionDetail(id);
        if (promotion == null) {
            return null;
        }

        // 创建视图对象
        PromotionDetailVO detailVO = new PromotionDetailVO();
        BeanUtils.copyProperties(promotion, detailVO);

        // 处理状态文本
        if (detailVO.getStatus() != null) {
            switch (detailVO.getStatus()) {
                case 0:
                    detailVO.setStatusText("待审核");
                    break;
                case 1:
                    detailVO.setStatusText("已通过");
                    break;
                case 2:
                    detailVO.setStatusText("已拒绝");
                    break;
                default:
                    detailVO.setStatusText("未知");
            }
        }

        // 处理附件列表
        if (StringUtils.hasText(promotion.getAttachmentUrls())) {
            detailVO.setAttachments(promotion.getAttachmentUrls().split(","));
        }

        // 获取企业信息
        if (promotion.getEnterpriseId() != null) {
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.getEnterpriseInfo(promotion.getEnterpriseId());
            if (enterpriseInfo != null) {
                detailVO.setEnterpriseName(enterpriseInfo.getEnterpriseName());
                detailVO.setEnterprisePhone(enterpriseInfo.getContactPhone());
                detailVO.setEnterpriseEmail(enterpriseInfo.getEmail());
                detailVO.setEnterpriseLogo(enterpriseInfo.getLogoPath());
                detailVO.setEnterpriseDescription(enterpriseInfo.getIntroduction());
            }
        }

        return detailVO;
    }
}