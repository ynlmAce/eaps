package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.EnterpriseInfoMapper;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.EnterpriseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 企业信息服务实现类
 */
@Service
public class EnterpriseInfoServiceImpl implements EnterpriseInfoService {

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 获取当前企业信息
     *
     * @return 企业信息
     */
    @Override
    public EnterpriseInfo getCurrentEnterpriseInfo() {
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
            throw new BusinessException("当前用户不是企业用户");
        }

        // 查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
        if (enterpriseInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        return enterpriseInfo;
    }

    /**
     * 更新企业信息
     *
     * @param enterpriseInfo 企业信息
     * @param license        营业执照文件（可选）
     * @param logo           企业Logo文件（可选）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnterpriseInfo(EnterpriseInfo enterpriseInfo, MultipartFile license, MultipartFile logo) {
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
            throw new BusinessException("当前用户不是企业用户");
        }

        // 查询当前企业信息
        EnterpriseInfo existInfo = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
        if (existInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 设置企业信息
        enterpriseInfo.setId(existInfo.getId());
        enterpriseInfo.setUserId(currentUser.getId());
        
        // 处理营业执照文件
        if (license != null && !license.isEmpty()) {
            String licensePath = uploadFile(license, "license");
            enterpriseInfo.setLicensePath(licensePath);
        } else {
            // 保持原有营业执照
            enterpriseInfo.setLicensePath(existInfo.getLicensePath());
        }

        // 处理企业Logo文件
        if (logo != null && !logo.isEmpty()) {
            String logoPath = uploadFile(logo, "logo");
            enterpriseInfo.setLogoPath(logoPath);
        } else {
            // 保持原有Logo
            enterpriseInfo.setLogoPath(existInfo.getLogoPath());
        }

        // 如果修改了重要信息，需要重新审核
        if (isImportantInfoChanged(existInfo, enterpriseInfo)) {
            enterpriseInfo.setVerifyStatus(0); // 待审核
            enterpriseInfo.setVerifier(null);
            enterpriseInfo.setVerifyTime(null);
            enterpriseInfo.setVerifyComment(null);
        } else {
            // 保持原有审核状态
            enterpriseInfo.setVerifyStatus(existInfo.getVerifyStatus());
            enterpriseInfo.setVerifier(existInfo.getVerifier());
            enterpriseInfo.setVerifyTime(existInfo.getVerifyTime());
            enterpriseInfo.setVerifyComment(existInfo.getVerifyComment());
        }

        // 更新企业信息
        enterpriseInfoMapper.updateById(enterpriseInfo);
    }

    /**
     * 获取企业列表
     *
     * @param params 查询参数
     * @return 企业列表与分页信息
     */
    @Override
    public Map<String, Object> getEnterpriseList(Map<String, Object> params) {
        // 构建查询条件
        LambdaQueryWrapper<EnterpriseInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 企业名称模糊查询
        String enterpriseName = (String) params.get("enterpriseName");
        if (StringUtils.hasText(enterpriseName)) {
            queryWrapper.like(EnterpriseInfo::getEnterpriseName, enterpriseName);
        }

        // 企业类型查询
        Integer enterpriseType = (Integer) params.get("enterpriseType");
        if (enterpriseType != null) {
            queryWrapper.eq(EnterpriseInfo::getEnterpriseType, enterpriseType);
        }

        // 企业规模查询
        Integer enterpriseSize = (Integer) params.get("enterpriseSize");
        if (enterpriseSize != null) {
            queryWrapper.eq(EnterpriseInfo::getEnterpriseSize, enterpriseSize);
        }

        // 行业查询
        String industry = (String) params.get("industry");
        if (StringUtils.hasText(industry)) {
            queryWrapper.like(EnterpriseInfo::getIndustry, industry);
        }

        // 默认只查询已审核通过的企业
        Integer verifyStatus = (Integer) params.get("verifyStatus");
        if (verifyStatus != null) {
            queryWrapper.eq(EnterpriseInfo::getVerifyStatus, verifyStatus);
        } else {
            queryWrapper.eq(EnterpriseInfo::getVerifyStatus, 1); // 已审核
        }

        // 查询总数
        long total = enterpriseInfoMapper.selectCount(queryWrapper);

        // 分页查询
        long pageNum = Long.parseLong(params.getOrDefault("pageNum", 1).toString());
        long pageSize = Long.parseLong(params.getOrDefault("pageSize", 10).toString());
        Page<EnterpriseInfo> page = new Page<>(pageNum, pageSize);
        List<EnterpriseInfo> enterpriseList = enterpriseInfoMapper.selectPage(page, queryWrapper).getRecords();

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", enterpriseList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 获取企业详情
     *
     * @param id 企业ID
     * @return 企业详情
     */
    @Override
    public Map<String, Object> getEnterpriseDetail(Long id) {
        // 查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectById(id);
        if (enterpriseInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String username = authentication.getName();
            User currentUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));

            // 如果是企业用户，只能查看自己的详情
            if (currentUser != null && currentUser.getUserType() == 3) {
                EnterpriseInfo userEnterpriseInfo = enterpriseInfoMapper.selectOne(
                        new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
                if (userEnterpriseInfo != null && !userEnterpriseInfo.getId().equals(id)) {
                    // 如果不是自己的企业信息，则只能查看已审核通过的企业
                    if (enterpriseInfo.getVerifyStatus() != 1) {
                        throw new BusinessException("该企业信息尚未审核通过，无法查看");
                    }
                }
            }
            // 如果不是企业用户和管理员，只能查看已审核通过的企业
            else if (currentUser != null && currentUser.getUserType() != 2) {
                if (enterpriseInfo.getVerifyStatus() != 1) {
                    throw new BusinessException("该企业信息尚未审核通过，无法查看");
                }
            }
        } else {
            // 未登录用户只能查看已审核通过的企业
            if (enterpriseInfo.getVerifyStatus() != 1) {
                throw new BusinessException("该企业信息尚未审核通过，无法查看");
            }
        }

        // 查询企业用户信息
        User enterpriseUser = userMapper.selectById(enterpriseInfo.getUserId());
        if (enterpriseUser != null) {
            enterpriseUser.setPassword(null); // 安全考虑，清除密码
        }

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("enterpriseInfo", enterpriseInfo);
        result.put("user", enterpriseUser);

        return result;
    }

    /**
     * 审核企业信息
     *
     * @param id           企业ID
     * @param verifyStatus 审核状态（1：通过，2：拒绝）
     * @param verifyComment 审核意见
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyEnterprise(Long id, Integer verifyStatus, String verifyComment) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查是否有审核权限
        if (currentUser.getUserType() != 2) {
            throw new BusinessException("当前用户无审核权限");
        }

        // 查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectById(id);
        if (enterpriseInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 检查是否为待审核状态
        if (enterpriseInfo.getVerifyStatus() != 0) {
            throw new BusinessException("该企业信息已审核，无需再次审核");
        }

        // 更新审核状态
        EnterpriseInfo updateInfo = new EnterpriseInfo();
        updateInfo.setId(id);
        updateInfo.setVerifyStatus(verifyStatus);
        updateInfo.setVerifier(username);
        updateInfo.setVerifyTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        updateInfo.setVerifyComment(verifyComment);

        enterpriseInfoMapper.updateById(updateInfo);
    }

    /**
     * 获取待审核的企业列表
     *
     * @param params 查询参数
     * @return 企业列表与分页信息
     */
    @Override
    public Map<String, Object> getPendingEnterpriseList(Map<String, Object> params) {
        // 设置查询条件为待审核
        params.put("verifyStatus", 0);
        return getEnterpriseList(params);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param type 文件类型（license：营业执照，logo：企业Logo）
     * @return 文件路径
     */
    private String uploadFile(MultipartFile file, String type) {
        try {
            // 生成文件保存路径
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String subDir = "enterprise/" + type;
            String filePath = uploadPath + File.separator + subDir + File.separator + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + File.separator + subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            file.transferTo(new File(filePath));
            return filePath;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否修改了重要信息（需要重新审核）
     *
     * @param oldInfo 原企业信息
     * @param newInfo 新企业信息
     * @return 是否修改了重要信息
     */
    private boolean isImportantInfoChanged(EnterpriseInfo oldInfo, EnterpriseInfo newInfo) {
        // 企业名称修改
        if (!oldInfo.getEnterpriseName().equals(newInfo.getEnterpriseName())) {
            return true;
        }
        
        // 企业法人修改
        if (!oldInfo.getLegalPerson().equals(newInfo.getLegalPerson())) {
            return true;
        }
        
        // 企业类型修改
        if (!oldInfo.getEnterpriseType().equals(newInfo.getEnterpriseType())) {
            return true;
        }
        
        // 营业执照修改
        if (newInfo.getLicensePath() != null && !oldInfo.getLicensePath().equals(newInfo.getLicensePath())) {
            return true;
        }
        
        return false;
    }
} 