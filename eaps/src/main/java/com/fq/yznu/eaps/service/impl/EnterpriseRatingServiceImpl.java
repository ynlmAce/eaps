package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fq.yznu.eaps.common.util.PageUtils;
import com.fq.yznu.eaps.entity.Enterprise;
import com.fq.yznu.eaps.entity.EnterpriseRating;
import com.fq.yznu.eaps.entity.Student;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.exception.BusinessException;
import com.fq.yznu.eaps.mapper.EnterpriseMapper;
import com.fq.yznu.eaps.mapper.EnterpriseRatingMapper;
import com.fq.yznu.eaps.mapper.StudentMapper;
import com.fq.yznu.eaps.service.EnterpriseRatingService;
import com.fq.yznu.eaps.service.UserService;
import com.fq.yznu.eaps.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业评分服务实现类
 */
@Service
@RequiredArgsConstructor
public class EnterpriseRatingServiceImpl extends ServiceImpl<EnterpriseRatingMapper, EnterpriseRating> implements EnterpriseRatingService {

    private final UserService userService;

    @Autowired
    private EnterpriseMapper enterpriseMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRating(EnterpriseRating rating) {
        // 验证企业是否存在
        Enterprise enterprise = enterpriseMapper.selectById(rating.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException("企业不存在");
        }

        // 获取当前登录学生
        User currentUser = SecurityUtils.getCurrentUser();
        Long studentId = currentUser.getId();

        // 检查学生是否已评价过该企业
        LambdaQueryWrapper<EnterpriseRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnterpriseRating::getEnterpriseId, rating.getEnterpriseId())
                .eq(EnterpriseRating::getStudentId, studentId)
                .eq(EnterpriseRating::getDeleted, false);
        int count = count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("您已评价过该企业，不能重复评价");
        }

        // 设置评分信息
        rating.setStudentId(studentId);
        rating.setStatus(0); // 待审核
        rating.setHelpfulCount(0);
        rating.setDeleted(0);
        rating.setCreateTime(LocalDateTime.now());
        rating.setUpdateTime(LocalDateTime.now());
        
        return this.save(rating);
    }

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
        rating.setUpdateTime(new Date());
        
        return this.updateById(rating);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRating(Long id) {
        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, id)
                .set(EnterpriseRating::getDeleted, 1)
                .set(EnterpriseRating::getUpdateTime, new Date()));
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
                .set(EnterpriseRating::getReplyTime, new Date())
                .set(EnterpriseRating::getUpdateTime, new Date()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean appealRating(RatingAppealReq appealReq) {
        EnterpriseRating rating = this.getById(appealReq.getRatingId());
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 只有通过审核的评价才能申诉
        if (rating.getStatus() != 1) {
            throw new RuntimeException("只有通过审核的评价才能申诉");
        }
        
        // 已经申诉过的不能重复申诉
        if (rating.getAppealStatus() != 0 && rating.getAppealStatus() != 3) {
            throw new RuntimeException("已经申诉过的评价不能重复申诉");
        }
        
        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, appealReq.getRatingId())
                .set(EnterpriseRating::getAppealStatus, 1) // 申诉中
                .set(EnterpriseRating::getAppealReason, appealReq.getAppealReason())
                .set(EnterpriseRating::getAppealEvidence, appealReq.getAppealEvidence())
                .set(EnterpriseRating::getEvidenceUrls, appealReq.getEvidenceUrls())
                .set(EnterpriseRating::getUpdateTime, new Date()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRating(RatingReviewReq reviewReq) {
        EnterpriseRating rating = this.getById(reviewReq.getRatingId());
        if (rating == null) {
            throw new RuntimeException("评价不存在");
        }
        
        return this.update(new LambdaUpdateWrapper<EnterpriseRating>()
                .eq(EnterpriseRating::getId, reviewReq.getRatingId())
                .set(EnterpriseRating::getStatus, reviewReq.getStatus())
                .set(EnterpriseRating::getReviewRemark, reviewReq.getReviewRemark())
                .set(EnterpriseRating::getUpdateTime, new Date()));
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
        Long enterpriseId = params.get("enterpriseId") != null ? Long.parseLong(params.get("enterpriseId").toString()) : null;
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
    public Page<Map<String, Object>> getEnterpriseRatingList(Long enterpriseId, Map<String, Object> params) {
        params.put("enterpriseId", enterpriseId);
        params.put("status", 1); // 只显示已通过的评价
        return this.pageRatingList(params);
    }

    @Override
    public Page<Map<String, Object>> getPendingRatingList(Map<String, Object> params) {
        params.put("status", 0); // 待审核
        return this.pageRatingList(params);
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
        
        // TODO: 记录用户标记，防止重复标记（可以使用Redis或单独的表）
        
        return baseMapper.incrementHelpfulCount(id) > 0;
    }

    @Override
    public Integer countPendingReview() {
        return baseMapper.countPendingReview();
    }
} 