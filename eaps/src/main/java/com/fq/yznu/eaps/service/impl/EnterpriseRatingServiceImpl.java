package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fq.yznu.eaps.vo.RatingAppealReq;
import com.fq.yznu.eaps.vo.RatingReviewReq;

import com.fq.yznu.eaps.entity.EnterpriseRating;
import com.fq.yznu.eaps.entity.RatingHelpfulMark;

import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.exception.BusinessException;

import com.fq.yznu.eaps.mapper.EnterpriseRatingMapper;
import com.fq.yznu.eaps.mapper.RatingHelpfulMarkMapper;
import com.fq.yznu.eaps.mapper.EnterpriseInfoMapper;

import com.fq.yznu.eaps.service.EnterpriseRatingService;

import com.fq.yznu.eaps.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fq.yznu.eaps.dto.EnterpriseRatingDTO;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.enums.RatingStatus;

/**
 * 企业评分服务实现类
 */
@Service
@RequiredArgsConstructor
public class EnterpriseRatingServiceImpl extends ServiceImpl<EnterpriseRatingMapper, EnterpriseRating>
        implements EnterpriseRatingService {

    private final RatingHelpfulMarkMapper ratingHelpfulMarkMapper;
    private final EnterpriseInfoMapper enterpriseInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRating(EnterpriseRating rating) {
        // 获取原评分
        EnterpriseRating original = this.getById(rating.getId());
        if (original == null) {
            throw new BusinessException("评分不存在");
        }

        // 验证是否为评分所有者
        EnterpriseRating existingRating = this.getById(rating.getId());
        if (existingRating == null) {
            throw new RuntimeException("评价不存在");
        }

        // 只有待审核或被拒绝的评价才能修改
        if (existingRating.getStatus() != 0 && existingRating.getStatus() != 2) {
            throw new RuntimeException("只有待审核或被拒绝的评价才能修改");
        }

        // 只能修改自己的评价
        if (!existingRating.getStudentId().equals(rating.getStudentId())) {
            throw new RuntimeException("只能修改自己的评价");
        }

        // 修改后重新设置为待审核状态
        rating.setStatus(0);
        rating.setUpdateTime(LocalDateTime.now());

        return this.updateById(rating);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRating(Long id) {
        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, id)
                .set(EnterpriseRating::getDeleted, 1)
                .set(EnterpriseRating::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRating(Long id) {
        // 直接调用removeRating方法，保持逻辑一致
        return removeRating(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyRating(Long id, String reply) {
        EnterpriseRating rating = this.getById(id);
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }

        // 只有已通过的评价才能回复
        if (rating.getStatus() != 1) {
            throw new RuntimeException("只有已通过的评价才能回复");
        }

        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, id)
                .set(EnterpriseRating::getReply, reply)
                .set(EnterpriseRating::getReplyTime, LocalDateTime.now())
                .set(EnterpriseRating::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean appealRating(RatingAppealReq appealReq) {
        EnterpriseRating rating = this.getById(appealReq.getId());
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }

        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, appealReq.getId())
                .set(EnterpriseRating::getStatus, 3) // 3-申诉中
                .set(EnterpriseRating::getAppealComment, appealReq.getAppealComment())
                .set(EnterpriseRating::getAppealEvidence, appealReq.getAppealEvidence())
                .set(EnterpriseRating::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRating(RatingReviewReq reviewReq) {
        EnterpriseRating rating = this.getById(reviewReq.getId());
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }

        Integer status = reviewReq.getApproved() ? 1 : 2; // 1-通过，2-拒绝
        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, reviewReq.getId())
                .set(EnterpriseRating::getStatus, status)
                .set(EnterpriseRating::getReviewRemark, reviewReq.getReviewComment())
                .set(EnterpriseRating::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public EnterpriseRating getRatingDetail(Long id) {
        EnterpriseRating rating = this.getById(id);
        if (rating == null) {
            return null;
        }

        // 如果是匿名评价，则隐藏学生ID
        if (rating.getAnonymous() == 1) {
            rating.setStudentId(null);
        }

        return rating;
    }

    @Override
    public Page<Map<String, Object>> pageRatingList(Map<String, Object> params) {
        Integer page = Integer.parseInt(params.getOrDefault("page", "1").toString());
        Integer limit = Integer.parseInt(params.getOrDefault("limit", "10").toString());
        Long enterpriseId = params.get("enterpriseId") != null ? Long.parseLong(params.get("enterpriseId").toString())
                : null;
        Long studentId = params.get("studentId") != null ? Long.parseLong(params.get("studentId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        String keyword = (String) params.get("keyword");

        Page<Map<String, Object>> pageParams = new Page<>(page, limit);
        return baseMapper.selectRatingPage(pageParams, enterpriseId, studentId, status, keyword);
    }

    @Override
    public Page<Map<String, Object>> getStudentRatingList(Long studentId, Map<String, Object> params) {
        params.put("studentId", studentId);
        return this.pageRatingList(params);
    }

    @Override
    public Page<Map<String, Object>> getStudentRatingList(Integer pageNum, Integer pageSize) {
        // 从当前登录用户获取学生ID
        User currentUser = SecurityUtils.getCurrentUser();
        Long studentId = currentUser.getId();

        // 构造参数
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);

        return getStudentRatingList(studentId, params);
    }

    @Override
    public Page<Map<String, Object>> getEnterpriseRatingList(Long enterpriseId, Map<String, Object> params) {
        params.put("enterpriseId", enterpriseId);
        params.put("status", 1); // 只显示已通过的评价
        return this.pageRatingList(params);
    }

    @Override
    public Page<Map<String, Object>> getEnterpriseRatingList(Integer pageNum, Integer pageSize) {
        // 从当前登录用户获取企业ID
        User currentUser = SecurityUtils.getCurrentUser();
        Long enterpriseId = currentUser.getId();

        // 构造参数
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);

        return getEnterpriseRatingList(enterpriseId, params);
    }

    @Override
    public Page<Map<String, Object>> getPendingRatingList(Map<String, Object> params) {
        params.put("status", 0); // 待审核
        return this.pageRatingList(params);
    }

    @Override
    public Page<Map<String, Object>> getPendingRatingList(Integer pageNum, Integer pageSize, Integer type) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        params.put("type", type);

        return getPendingRatingList(params);
    }

    @Override
    public Map<String, Object> getEnterpriseAverageRating(Long enterpriseId) {
        return baseMapper.selectEnterpriseAverageRating(enterpriseId);
    }

    @Override
    public List<Map<String, Object>> getHotRatingList(Long enterpriseId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5; // 默认显示5条
        }
        return baseMapper.selectHotRatingList(enterpriseId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markRatingHelpful(Long id, Long userId) {
        EnterpriseRating rating = this.getById(id);
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }

        // 只有已通过的评价才能标记有用
        if (rating.getStatus() != 1) {
            throw new RuntimeException("只有已通过的评价才能标记有用");
        }

        // 检查是否已经标记过
        LambdaQueryWrapper<RatingHelpfulMark> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RatingHelpfulMark::getRatingId, id)
                .eq(RatingHelpfulMark::getUserId, userId);
        if (ratingHelpfulMarkMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("您已经标记过该评价");
        }

        // 记录标记
        RatingHelpfulMark mark = new RatingHelpfulMark();
        mark.setRatingId(id);
        mark.setUserId(userId);
        mark.setCreateTime(LocalDateTime.now());
        ratingHelpfulMarkMapper.insert(mark);

        return baseMapper.incrementHelpfulCount(id) > 0;
    }

    @Override
    public Integer countPendingReview() {
        return baseMapper.countPendingReview();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewAppeal(Long id, Integer appealStatus, String appealReviewComment) {
        EnterpriseRating rating = this.getById(id);
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }

        // 只有处于申诉中的评价才能审核
        if (rating.getAppealStatus() != 1) {
            throw new RuntimeException("只有处于申诉中的评价才能审核");
        }

        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, id)
                .set(EnterpriseRating::getAppealStatus, appealStatus)
                .set(EnterpriseRating::getReviewRemark, appealReviewComment)
                .set(EnterpriseRating::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAppeal(Long id, String appealContent, String attachments) {
        EnterpriseRating rating = this.getById(id);
        if (rating == null) {
            throw new BusinessException("评价不存在");
        }

        // 只有被拒绝的评价才能申诉
        if (rating.getStatus() != 2) {
            throw new BusinessException("只有被拒绝的评价才能申诉");
        }

        // 创建申诉请求对象
        RatingAppealReq appealReq = new RatingAppealReq();
        appealReq.setId(id);
        appealReq.setAppealComment(appealContent);
        appealReq.setAppealEvidence(attachments);

        // 调用已有的申诉方法
        return appealRating(appealReq);
    }

    @Override
    public Page<Map<String, Object>> getRatingList(Long enterpriseId, Integer pageNum, Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        params.put("enterpriseId", enterpriseId);
        params.put("status", 1); // 只显示已通过的评价

        return pageRatingList(params);
    }

    @Override
    public EnterpriseRating getRatingById(Long id) {
        return getRatingDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRating(EnterpriseRatingDTO ratingDTO) {
        // 验证企业是否存在
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectById(ratingDTO.getEnterpriseId());
        if (enterpriseInfo == null) {
            throw new BusinessException("企业不存在");
        }

        // 获取当前登录学生
        Long studentId = SecurityUtils.getLoginUserId();

        // 检查是否已经评价过
        LambdaQueryWrapper<EnterpriseRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnterpriseRating::getEnterpriseId, ratingDTO.getEnterpriseId())
                .eq(EnterpriseRating::getStudentId, studentId);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("您已经评价过该企业");
        }

        // 转换DTO为实体并保存
        EnterpriseRating rating = new EnterpriseRating();
        rating.setEnterpriseId(ratingDTO.getEnterpriseId());
        rating.setStudentId(studentId);
        rating.setRating(ratingDTO.getRating());
        rating.setContent(ratingDTO.getContent());
        rating.setStatus(RatingStatus.PENDING.getCode());
        rating.setCreateTime(LocalDateTime.now());
        rating.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(rating);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRating(Long id, Integer status, String reviewComment) {
        // 创建审核请求对象
        RatingReviewReq reviewReq = new RatingReviewReq();
        reviewReq.setId(id);
        reviewReq.setApproved(status == 1);
        reviewReq.setReviewComment(reviewComment);

        // 调用主要的审核方法
        return reviewRating(reviewReq);
    }
}