package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.entity.JobPosition;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.EnterpriseInfoMapper;
import com.fq.yznu.eaps.mapper.JobPositionMapper;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.JobPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职位服务实现类
 */
@Service
public class JobPositionServiceImpl implements JobPositionService {

    @Autowired
    private JobPositionMapper jobPositionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    /**
     * 发布职位
     *
     * @param jobPosition 职位信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPosition(JobPosition jobPosition) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查当前用户是否是企业用户
        if (currentUser.getUserType() != 3) {
            throw new BusinessException("只有企业用户才能发布职位");
        }

        // 查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
        if (enterpriseInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 设置职位信息
        jobPosition.setEnterpriseId(enterpriseInfo.getId());
        jobPosition.setPublishTime(LocalDateTime.now());
        jobPosition.setStatus(0); // 0: 待审核

        // 保存职位信息
        jobPositionMapper.insert(jobPosition);
    }

    /**
     * 更新职位信息
     *
     * @param jobPosition 职位信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePosition(JobPosition jobPosition) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 查询职位信息
        JobPosition existPosition = jobPositionMapper.selectById(jobPosition.getId());
        if (existPosition == null) {
            throw new BusinessException("职位不存在");
        }

        // 检查当前用户是否有权限修改此职位
        if (currentUser.getUserType() == 3) { // 企业用户
            // 查询企业信息
            EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
            if (enterpriseInfo == null) {
                throw new BusinessException("企业信息不存在");
            }

            // 检查职位是否属于该企业
            if (!existPosition.getEnterpriseId().equals(enterpriseInfo.getId())) {
                throw new BusinessException("无权修改其他企业的职位");
            }

            // 如果职位已审核通过，则修改后需重新审核
            if (existPosition.getStatus() == 1) {
                jobPosition.setStatus(0); // 0: 待审核
            }
        } else if (currentUser.getUserType() == 2) { // 管理员
            // 管理员可以修改所有职位信息
        } else {
            throw new BusinessException("无权修改职位信息");
        }

        // 不能修改的字段
        jobPosition.setEnterpriseId(existPosition.getEnterpriseId());
        jobPosition.setPublishTime(existPosition.getPublishTime());

        // 更新职位信息
        jobPositionMapper.updateById(jobPosition);
    }

    /**
     * 删除职位
     *
     * @param id 职位ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePosition(Long id) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 查询职位信息
        JobPosition existPosition = jobPositionMapper.selectById(id);
        if (existPosition == null) {
            throw new BusinessException("职位不存在");
        }

        // 检查当前用户是否有权限删除此职位
        if (currentUser.getUserType() == 3) { // 企业用户
            // 查询企业信息
            EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
            if (enterpriseInfo == null) {
                throw new BusinessException("企业信息不存在");
            }

            // 检查职位是否属于该企业
            if (!existPosition.getEnterpriseId().equals(enterpriseInfo.getId())) {
                throw new BusinessException("无权删除其他企业的职位");
            }
        } else if (currentUser.getUserType() == 2) { // 管理员
            // 管理员可以删除所有职位
        } else {
            throw new BusinessException("无权删除职位");
        }

        // 删除职位
        jobPositionMapper.deleteById(id);
    }

    /**
     * 审核职位
     *
     * @param id     职位ID
     * @param status 审核状态（1：通过，2：拒绝）
     * @param reason 拒绝原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPosition(Long id, Integer status, String reason) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 只有管理员才能审核职位
        if (currentUser.getUserType() != 2) {
            throw new BusinessException("只有管理员才能审核职位");
        }

        // 查询职位信息
        JobPosition existPosition = jobPositionMapper.selectById(id);
        if (existPosition == null) {
            throw new BusinessException("职位不存在");
        }

        // 只能审核待审核的职位
        if (existPosition.getStatus() != 0) {
            throw new BusinessException("只能审核待审核的职位");
        }

        // 更新职位状态
        JobPosition updatePosition = new JobPosition();
        updatePosition.setId(id);
        updatePosition.setStatus(status);
        if (status == 2 && StringUtils.hasText(reason)) {
            updatePosition.setReviewComment(reason);
        }

        jobPositionMapper.updateById(updatePosition);
    }

    /**
     * 获取职位列表
     *
     * @param queryParams 查询参数
     * @return 职位列表与分页信息
     */
    @Override
    public Map<String, Object> getPositionList(Map<String, Object> queryParams) {
        // 构建查询条件
        LambdaQueryWrapper<JobPosition> queryWrapper = new LambdaQueryWrapper<>();

        // 职位名称模糊查询
        String positionName = (String) queryParams.get("positionName");
        if (StringUtils.hasText(positionName)) {
            queryWrapper.like(JobPosition::getPositionName, positionName);
        }

        // 企业ID精确查询
        Long enterpriseId = (Long) queryParams.get("enterpriseId");
        if (enterpriseId != null) {
            queryWrapper.eq(JobPosition::getEnterpriseId, enterpriseId);
        }

        // 职位状态查询
        Integer status = (Integer) queryParams.get("status");
        if (status != null) {
            queryWrapper.eq(JobPosition::getStatus, status);
        } else {
            // 默认只查询审核通过的职位
            queryWrapper.eq(JobPosition::getStatus, 1);
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String username = authentication.getName();
            User currentUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));

            // 如果是企业用户，只能查看自己发布的职位
            if (currentUser != null && currentUser.getUserType() == 3) {
                EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                        new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
                if (enterpriseInfo != null) {
                    queryWrapper.eq(JobPosition::getEnterpriseId, enterpriseInfo.getId());
                }
            }
        }

        // 按发布时间降序排序
        queryWrapper.orderByDesc(JobPosition::getPublishTime);

        // 查询总数
        long total = jobPositionMapper.selectCount(queryWrapper);

        // 分页查询
        long pageNum = Long.parseLong(queryParams.getOrDefault("pageNum", 1).toString());
        long pageSize = Long.parseLong(queryParams.getOrDefault("pageSize", 10).toString());
        Page<JobPosition> page = new Page<>(pageNum, pageSize);
        List<JobPosition> positionList = jobPositionMapper.selectPage(page, queryWrapper).getRecords();

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", positionList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 获取职位详情
     *
     * @param id 职位ID
     * @return 职位详情
     */
    @Override
    public JobPosition getPositionDetail(Long id) {
        // 查询职位信息
        JobPosition jobPosition = jobPositionMapper.selectById(id);
        if (jobPosition == null) {
            throw new BusinessException("职位不存在");
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String username = authentication.getName();
            User currentUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));

            // 如果是企业用户，只能查看自己发布的职位
            if (currentUser != null && currentUser.getUserType() == 3) {
                EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                        new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
                if (enterpriseInfo != null && !jobPosition.getEnterpriseId().equals(enterpriseInfo.getId())) {
                    // 如果不是自己发布的职位，并且状态不是已审核，则无权查看
                    if (jobPosition.getStatus() != 1) {
                        throw new BusinessException("无权查看此职位");
                    }
                }
            }
            // 如果是学生或辅导员，只能查看已审核的职位
            else if (currentUser != null && (currentUser.getUserType() == 0 || currentUser.getUserType() == 4)) {
                if (jobPosition.getStatus() != 1) {
                    throw new BusinessException("职位不存在或未审核");
                }
            }
        } else {
            // 未登录用户只能查看已审核的职位
            if (jobPosition.getStatus() != 1) {
                throw new BusinessException("职位不存在或未审核");
            }
        }

        return jobPosition;
    }
}