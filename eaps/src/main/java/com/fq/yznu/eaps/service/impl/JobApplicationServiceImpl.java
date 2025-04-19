package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.entity.JobApplication;
import com.fq.yznu.eaps.entity.JobPosition;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.EnterpriseInfoMapper;
import com.fq.yznu.eaps.mapper.JobApplicationMapper;
import com.fq.yznu.eaps.mapper.JobPositionMapper;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.JobApplicationService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 职位申请服务实现类
 */
@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    @Autowired
    private JobApplicationMapper jobApplicationMapper;

    @Autowired
    private JobPositionMapper jobPositionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 提交职位申请
     *
     * @param application 申请信息
     * @param resume      简历文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(JobApplication application, MultipartFile resume) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查当前用户是否是学生
        if (currentUser.getUserType() != 0) {
            throw new BusinessException("只有学生用户才能申请职位");
        }

        // 查询职位信息
        JobPosition jobPosition = jobPositionMapper.selectById(application.getJobId());
        if (jobPosition == null) {
            throw new BusinessException("职位不存在");
        }

        // 检查职位是否已审核通过
        if (jobPosition.getStatus() != 1) {
            throw new BusinessException("该职位尚未审核通过，不能申请");
        }

        // 检查是否已经申请过该职位
        LambdaQueryWrapper<JobApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobApplication::getJobId, application.getJobId());
        queryWrapper.eq(JobApplication::getStudentId, currentUser.getId());
        // 排除已撤销的申请
        queryWrapper.ne(JobApplication::getStatus, 5); // 5表示已撤销
        
        Long count = jobApplicationMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("您已申请过该职位，不能重复申请");
        }

        // 设置申请信息
        application.setStudentId(currentUser.getId());
        application.setEnterpriseId(jobPosition.getEnterpriseId());
        application.setStatus(0); // 0: 已申请
        application.setApplyTime(LocalDateTime.now());

        // 处理简历
        if (resume != null && !resume.isEmpty()) {
            // 设置简历类型为附件上传
            application.setResumeType(1);

            // 生成文件保存路径
            String originalFilename = resume.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = uploadPath + File.separator + "resumes" + File.separator + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + File.separator + "resumes");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            try {
                resume.transferTo(new File(filePath));
                application.setResumePath(filePath);
            } catch (IOException e) {
                throw new BusinessException("简历上传失败: " + e.getMessage());
            }
        } else {
            // 设置简历类型为在线填写
            application.setResumeType(0);
            
            // 检查简历内容是否存在
            if (!StringUtils.hasText(application.getResumeContent())) {
                throw new BusinessException("请填写简历内容");
            }
        }

        // 保存申请信息
        jobApplicationMapper.insert(application);

        // 更新职位申请人数
        JobPosition updatePosition = new JobPosition();
        updatePosition.setId(jobPosition.getId());
        updatePosition.setApplyCount(jobPosition.getApplyCount() + 1);
        jobPositionMapper.updateById(updatePosition);
    }

    /**
     * 获取我的申请列表
     *
     * @param params 查询参数
     * @return 申请列表与分页信息
     */
    @Override
    public Map<String, Object> getMyApplicationList(Map<String, Object> params) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 构建查询条件
        LambdaQueryWrapper<JobApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobApplication::getStudentId, currentUser.getId());

        // 按申请状态查询
        Integer status = (Integer) params.get("status");
        if (status != null) {
            queryWrapper.eq(JobApplication::getStatus, status);
        }

        // 按职位名称查询
        String positionName = (String) params.get("positionName");
        if (StringUtils.hasText(positionName)) {
            List<JobPosition> positions = jobPositionMapper.selectList(
                    new LambdaQueryWrapper<JobPosition>().like(JobPosition::getPositionName, positionName));
            if (!positions.isEmpty()) {
                Long[] positionIds = positions.stream().map(JobPosition::getId).toArray(Long[]::new);
                queryWrapper.in(JobApplication::getJobId, (Object[]) positionIds);
            } else {
                // 没有匹配的职位，直接返回空结果
                Map<String, Object> result = new HashMap<>();
                result.put("total", 0);
                result.put("list", List.of());
                result.put("pageNum", 1);
                result.put("pageSize", 10);
                return result;
            }
        }

        // 按申请时间降序排序
        queryWrapper.orderByDesc(JobApplication::getApplyTime);

        // 查询总数
        long total = jobApplicationMapper.selectCount(queryWrapper);

        // 分页查询
        long pageNum = Long.parseLong(params.getOrDefault("pageNum", 1).toString());
        long pageSize = Long.parseLong(params.getOrDefault("pageSize", 10).toString());
        Page<JobApplication> page = new Page<>(pageNum, pageSize);
        List<JobApplication> applicationList = jobApplicationMapper.selectPage(page, queryWrapper).getRecords();

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", applicationList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 获取申请详情
     *
     * @param id 申请ID
     * @return 申请详情
     */
    @Override
    public Map<String, Object> getApplicationDetail(Long id) {
        // 查询申请信息
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 验证权限
        if (currentUser.getUserType() == 0) { // 学生
            // 学生只能查看自己的申请
            if (!application.getStudentId().equals(currentUser.getId())) {
                throw new BusinessException("无权查看此申请");
            }
        } else if (currentUser.getUserType() == 3) { // 企业
            // 企业只能查看发给自己企业的申请
            EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
            if (enterpriseInfo == null || !application.getEnterpriseId().equals(enterpriseInfo.getId())) {
                throw new BusinessException("无权查看此申请");
            }
            
            // 如果企业第一次查看此申请，更新查看时间和状态
            if (application.getStatus() == 0 && application.getViewTime() == null) {
                JobApplication updateApplication = new JobApplication();
                updateApplication.setId(id);
                updateApplication.setStatus(1); // 1: 已查看
                updateApplication.setViewTime(LocalDateTime.now());
                jobApplicationMapper.updateById(updateApplication);
                
                // 更新本地对象，用于返回
                application.setStatus(1);
                application.setViewTime(LocalDateTime.now());
            }
        } else if (currentUser.getUserType() != 2) { // 非管理员
            throw new BusinessException("无权查看此申请");
        }

        // 查询职位信息
        JobPosition jobPosition = jobPositionMapper.selectById(application.getJobId());

        // 查询学生信息
        User student = userMapper.selectById(application.getStudentId());
        if (student != null) {
            student.setPassword(null); // 安全考虑，清除密码
        }

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("application", application);
        result.put("jobPosition", jobPosition);
        result.put("student", student);

        return result;
    }

    /**
     * 撤销申请
     *
     * @param id 申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(Long id) {
        // 查询申请信息
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 验证权限
        if (currentUser.getUserType() != 0 || !application.getStudentId().equals(currentUser.getId())) {
            throw new BusinessException("无权撤销此申请");
        }

        // 检查申请状态，只有已申请和已查看状态可以撤销
        if (application.getStatus() > 1) {
            throw new BusinessException("该申请已被企业处理，无法撤销");
        }

        // 更新申请状态
        JobApplication updateApplication = new JobApplication();
        updateApplication.setId(id);
        updateApplication.setStatus(5); // 5: 已撤销
        jobApplicationMapper.updateById(updateApplication);

        // 更新职位申请人数
        JobPosition jobPosition = jobPositionMapper.selectById(application.getJobId());
        if (jobPosition != null && jobPosition.getApplyCount() > 0) {
            JobPosition updatePosition = new JobPosition();
            updatePosition.setId(jobPosition.getId());
            updatePosition.setApplyCount(jobPosition.getApplyCount() - 1);
            jobPositionMapper.updateById(updatePosition);
        }
    }

    /**
     * 获取企业的申请列表
     *
     * @param params 查询参数
     * @return 申请列表与分页信息
     */
    @Override
    public Map<String, Object> getEnterpriseApplicationList(Map<String, Object> params) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查是否是企业用户
        if (currentUser.getUserType() != 3) {
            throw new BusinessException("只有企业用户才能查看企业申请列表");
        }

        // 查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
        if (enterpriseInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 构建查询条件
        LambdaQueryWrapper<JobApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobApplication::getEnterpriseId, enterpriseInfo.getId());

        // 按职位ID查询
        Long jobId = params.get("jobId") != null ? Long.parseLong(params.get("jobId").toString()) : null;
        if (jobId != null) {
            queryWrapper.eq(JobApplication::getJobId, jobId);
        }

        // 按申请状态查询
        Integer status = (Integer) params.get("status");
        if (status != null) {
            queryWrapper.eq(JobApplication::getStatus, status);
        }

        // 按申请时间降序排序
        queryWrapper.orderByDesc(JobApplication::getApplyTime);

        // 查询总数
        long total = jobApplicationMapper.selectCount(queryWrapper);

        // 分页查询
        long pageNum = Long.parseLong(params.getOrDefault("pageNum", 1).toString());
        long pageSize = Long.parseLong(params.getOrDefault("pageSize", 10).toString());
        Page<JobApplication> page = new Page<>(pageNum, pageSize);
        List<JobApplication> applicationList = jobApplicationMapper.selectPage(page, queryWrapper).getRecords();

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", applicationList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 更新申请状态
     *
     * @param id          申请ID
     * @param status      状态
     * @param feedback    反馈信息
     * @param extraParams 额外参数，如面试信息等
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplicationStatus(Long id, Integer status, String feedback, Map<String, Object> extraParams) {
        // 查询申请信息
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查是否是企业用户
        if (currentUser.getUserType() != 3) {
            throw new BusinessException("只有企业用户才能更新申请状态");
        }

        // 查询企业信息
        EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, currentUser.getId()));
        if (enterpriseInfo == null) {
            throw new BusinessException("企业信息不存在");
        }

        // 验证权限
        if (!application.getEnterpriseId().equals(enterpriseInfo.getId())) {
            throw new BusinessException("无权更新此申请");
        }

        // 准备更新对象
        JobApplication updateApplication = new JobApplication();
        updateApplication.setId(id);
        updateApplication.setStatus(status);
        updateApplication.setFeedback(feedback);
        updateApplication.setFeedbackTime(LocalDateTime.now());

        // 处理面试信息
        if (status == 2) { // 面试中
            // 设置面试时间
            String interviewTimeStr = (String) extraParams.get("interviewTime");
            if (StringUtils.hasText(interviewTimeStr)) {
                try {
                    LocalDateTime interviewTime = LocalDateTime.parse(interviewTimeStr);
                    updateApplication.setInterviewTime(interviewTime);
                } catch (Exception e) {
                    throw new BusinessException("面试时间格式错误");
                }
            }

            // 设置面试地点
            String interviewLocation = (String) extraParams.get("interviewLocation");
            updateApplication.setInterviewLocation(interviewLocation);

            // 设置面试联系人
            String interviewContact = (String) extraParams.get("interviewContact");
            updateApplication.setInterviewContact(interviewContact);

            // 设置面试联系电话
            String interviewPhone = (String) extraParams.get("interviewPhone");
            updateApplication.setInterviewPhone(interviewPhone);

            // 设置面试备注
            String interviewRemark = (String) extraParams.get("interviewRemark");
            updateApplication.setInterviewRemark(interviewRemark);
        }

        // 更新申请信息
        jobApplicationMapper.updateById(updateApplication);
    }
} 