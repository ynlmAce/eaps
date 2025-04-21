package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.entity.CounselorInfo;
import com.fq.yznu.eaps.entity.StudentInfo;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.CounselorInfoMapper;
import com.fq.yznu.eaps.mapper.StudentInfoMapper;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.StudentInfoService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生信息服务实现类
 */
@Service
public class StudentInfoServiceImpl implements StudentInfoService {

    @Autowired
    private StudentInfoMapper studentInfoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CounselorInfoMapper counselorInfoMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 获取当前学生信息
     *
     * @return 学生信息
     */
    @Override
    public Map<String, Object> getCurrentStudentInfo() {
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
            throw new BusinessException("当前用户不是学生用户");
        }

        // 查询学生信息
        StudentInfo studentInfo = studentInfoMapper.selectOne(
                new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, currentUser.getId()));
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 查询辅导员信息
        CounselorInfo counselorInfo = null;
        if (studentInfo.getCounselorId() != null) {
            counselorInfo = counselorInfoMapper.selectById(studentInfo.getCounselorId());
        }

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("studentInfo", studentInfo);
        result.put("user", currentUser);
        result.put("counselorInfo", counselorInfo);

        return result;
    }

    /**
     * 更新学生信息
     *
     * @param studentInfo 学生信息
     * @param resume      简历文件（可选）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentInfo(StudentInfo studentInfo, MultipartFile resume) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查是否为学生或管理员
        boolean isAdmin = currentUser.getUserType() == 2;
        boolean isStudent = currentUser.getUserType() == 0;

        if (!isStudent && !isAdmin) {
            throw new BusinessException("无权限更新学生信息");
        }

        // 获取原学生信息
        StudentInfo existInfo;
        if (isStudent) {
            // 学生只能更新自己的信息
            existInfo = studentInfoMapper.selectOne(
                    new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, currentUser.getId()));
            if (existInfo == null) {
                throw new BusinessException("学生信息不存在");
            }

            // 确保是更新自己的信息
            if (!existInfo.getId().equals(studentInfo.getId())) {
                throw new BusinessException("无权限更新其他学生信息");
            }

            // 某些字段学生不能修改
            studentInfo.setStudentNumber(existInfo.getStudentNumber());
            studentInfo.setRealName(existInfo.getRealName());
            studentInfo.setCollege(existInfo.getCollege());
            studentInfo.setMajor(existInfo.getMajor());
            studentInfo.setClassName(existInfo.getClassName());
            studentInfo.setEnrollmentYear(existInfo.getEnrollmentYear());
            studentInfo.setGraduationYear(existInfo.getGraduationYear());
            studentInfo.setEducation(existInfo.getEducation());
            studentInfo.setCounselorId(existInfo.getCounselorId());
        } else {
            // 管理员可以更新任何学生信息
            existInfo = studentInfoMapper.selectById(studentInfo.getId());
            if (existInfo == null) {
                throw new BusinessException("学生信息不存在");
            }
        }

        // 设置学生信息
        studentInfo.setUserId(existInfo.getUserId());

        // 处理简历文件
        if (resume != null && !resume.isEmpty()) {
            String resumePath = uploadFile(resume);
            studentInfo.setResumePath(resumePath);
        } else {
            // 保持原有简历
            studentInfo.setResumePath(existInfo.getResumePath());
        }

        // 更新学生信息
        studentInfoMapper.updateById(studentInfo);

        // 如果是管理员更新了就业状态，则同步更新用户表相关字段
        if (isAdmin && studentInfo.getEmploymentStatus() != null &&
                !studentInfo.getEmploymentStatus().equals(existInfo.getEmploymentStatus())) {
            User userToUpdate = new User();
            userToUpdate.setId(existInfo.getUserId());
            // 可以在这里同步更新用户表中的相关字段
            userMapper.updateById(userToUpdate);
        }
    }

    /**
     * 获取学生列表
     *
     * @param params 查询参数
     * @return 学生列表与分页信息
     */
    @Override
    public Map<String, Object> getStudentList(Map<String, Object> params) {
        // 构建查询条件
        LambdaQueryWrapper<StudentInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 学生姓名模糊查询
        String realName = (String) params.get("realName");
        if (StringUtils.hasText(realName)) {
            queryWrapper.like(StudentInfo::getRealName, realName);
        }

        // 学号查询
        String studentId = (String) params.get("studentId");
        if (StringUtils.hasText(studentId)) {
            queryWrapper.eq(StudentInfo::getStudentNumber, studentId);
        }

        // 学院查询
        String college = (String) params.get("college");
        if (StringUtils.hasText(college)) {
            queryWrapper.eq(StudentInfo::getCollege, college);
        }

        // 专业查询
        String major = (String) params.get("major");
        if (StringUtils.hasText(major)) {
            queryWrapper.eq(StudentInfo::getMajor, major);
        }

        // 班级查询
        String className = (String) params.get("className");
        if (StringUtils.hasText(className)) {
            queryWrapper.eq(StudentInfo::getClassName, className);
        }

        // 入学年份查询
        String enrollmentYear = (String) params.get("enrollmentYear");
        if (StringUtils.hasText(enrollmentYear)) {
            queryWrapper.eq(StudentInfo::getEnrollmentYear, enrollmentYear);
        }

        // 毕业年份查询
        String graduationYear = (String) params.get("graduationYear");
        if (StringUtils.hasText(graduationYear)) {
            queryWrapper.eq(StudentInfo::getGraduationYear, graduationYear);
        }

        // 学历层次查询
        Integer educationLevel = (Integer) params.get("educationLevel");
        if (educationLevel != null) {
            queryWrapper.eq(StudentInfo::getEducation, educationLevel);
        }

        // 就业状态查询
        Integer employmentStatus = (Integer) params.get("employmentStatus");
        if (employmentStatus != null) {
            queryWrapper.eq(StudentInfo::getEmploymentStatus, employmentStatus);
        }

        // 辅导员ID查询
        Long counselorId = params.get("counselorId") != null ? Long.parseLong(params.get("counselorId").toString())
                : null;
        if (counselorId != null) {
            queryWrapper.eq(StudentInfo::getCounselorId, counselorId);
        }

        // 查询总数
        long total = studentInfoMapper.selectCount(queryWrapper);

        // 分页查询
        long pageNum = Long.parseLong(params.getOrDefault("pageNum", 1).toString());
        long pageSize = Long.parseLong(params.getOrDefault("pageSize", 10).toString());
        Page<StudentInfo> page = new Page<>(pageNum, pageSize);
        List<StudentInfo> studentList = studentInfoMapper.selectPage(page, queryWrapper).getRecords();

        // 查询用户信息
        List<Long> userIds = studentList.stream()
                .map(StudentInfo::getUserId)
                .collect(Collectors.toList());

        List<User> userList = new ArrayList<>();
        if (!userIds.isEmpty()) {
            userList = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getId, userIds));
            // 安全考虑，清除密码
            userList.forEach(user -> user.setPassword(null));
        }

        // 构建学生详细信息
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (StudentInfo student : studentList) {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("studentInfo", student);

            // 查找对应的用户信息
            userList.stream()
                    .filter(user -> user.getId().equals(student.getUserId()))
                    .findFirst()
                    .ifPresent(user -> detailMap.put("user", user));

            detailList.add(detailMap);
        }

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", detailList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 获取学生详情
     *
     * @param id 学生ID
     * @return 学生详情
     */
    @Override
    public Map<String, Object> getStudentDetail(Long id) {
        // 查询学生信息
        StudentInfo studentInfo = studentInfoMapper.selectById(id);
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            String username = authentication.getName();
            User currentUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username));

            // 权限检查：只有自己、辅导员、管理员可以查看详情
            if (currentUser != null) {
                boolean isSelf = currentUser.getUserType() == 0 &&
                        studentInfo.getUserId().equals(currentUser.getId());
                boolean isCounselor = currentUser.getUserType() == 4;
                boolean isAdmin = currentUser.getUserType() == 2;

                if (!isSelf && !isAdmin) {
                    if (isCounselor) {
                        // 辅导员只能查看自己负责的学生
                        CounselorInfo counselorInfo = counselorInfoMapper.selectOne(
                                new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId,
                                        currentUser.getId()));
                        if (counselorInfo == null || !counselorInfo.getId().equals(studentInfo.getCounselorId())) {
                            throw new BusinessException("无权查看非本人负责的学生信息");
                        }
                    } else {
                        throw new BusinessException("无权查看该学生信息");
                    }
                }
            } else {
                throw new BusinessException("无权查看该学生信息");
            }
        } else {
            throw new BusinessException("无权查看该学生信息");
        }

        // 查询用户信息
        User user = userMapper.selectById(studentInfo.getUserId());
        if (user != null) {
            user.setPassword(null); // 安全考虑，清除密码
        }

        // 查询辅导员信息
        CounselorInfo counselorInfo = null;
        if (studentInfo.getCounselorId() != null) {
            counselorInfo = counselorInfoMapper.selectById(studentInfo.getCounselorId());
        }

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("studentInfo", studentInfo);
        result.put("user", user);
        result.put("counselorInfo", counselorInfo);

        return result;
    }

    /**
     * 获取辅导员的学生列表
     *
     * @param params 查询参数
     * @return 学生列表与分页信息
     */
    @Override
    public Map<String, Object> getCounselorStudentList(Map<String, Object> params) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查当前用户是否是辅导员
        if (currentUser.getUserType() != 4) {
            throw new BusinessException("当前用户不是辅导员");
        }

        // 查询辅导员信息
        CounselorInfo counselorInfo = counselorInfoMapper.selectOne(
                new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId, currentUser.getId()));
        if (counselorInfo == null) {
            throw new BusinessException("辅导员信息不存在");
        }

        // 设置辅导员ID参数
        params.put("counselorId", counselorInfo.getId());

        // 调用学生列表查询方法
        return getStudentList(params);
    }

    /**
     * 更新学生就业状态
     *
     * @param id               学生ID
     * @param employmentStatus 就业状态（0：未就业，1：已就业）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmploymentStatus(Long id, Integer employmentStatus) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 查询学生信息
        StudentInfo studentInfo = studentInfoMapper.selectById(id);
        if (studentInfo == null) {
            throw new BusinessException("学生信息不存在");
        }

        // 权限检查：只有自己、辅导员、管理员可以更新就业状态
        boolean isSelf = currentUser.getUserType() == 0 &&
                studentInfo.getUserId().equals(currentUser.getId());
        boolean isCounselor = currentUser.getUserType() == 4;
        boolean isAdmin = currentUser.getUserType() == 2;

        if (!isSelf && !isAdmin) {
            if (isCounselor) {
                // 辅导员只能更新自己负责的学生
                CounselorInfo counselorInfo = counselorInfoMapper.selectOne(
                        new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId, currentUser.getId()));
                if (counselorInfo == null || !counselorInfo.getId().equals(studentInfo.getCounselorId())) {
                    throw new BusinessException("无权更新非本人负责的学生就业状态");
                }
            } else {
                throw new BusinessException("无权更新学生就业状态");
            }
        }

        // 更新就业状态
        StudentInfo updateInfo = new StudentInfo();
        updateInfo.setId(id);
        updateInfo.setEmploymentStatus(employmentStatus);
        studentInfoMapper.updateById(updateInfo);
    }

    /**
     * 获取就业统计信息
     *
     * @param params 查询参数
     * @return 就业统计信息
     */
    @Override
    public Map<String, Object> getEmploymentStatistics(Map<String, Object> params) {
        // 统计信息结果
        Map<String, Object> result = new HashMap<>();

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        // 构建查询条件
        LambdaQueryWrapper<StudentInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 如果是辅导员，只统计自己负责的学生
        if (currentUser != null && currentUser.getUserType() == 4) {
            CounselorInfo counselorInfo = counselorInfoMapper.selectOne(
                    new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId, currentUser.getId()));
            if (counselorInfo != null) {
                queryWrapper.eq(StudentInfo::getCounselorId, counselorInfo.getId());
            }
        }

        // 筛选条件
        // 学院查询
        String college = (String) params.get("college");
        if (StringUtils.hasText(college)) {
            queryWrapper.eq(StudentInfo::getCollege, college);
        }

        // 专业查询
        String major = (String) params.get("major");
        if (StringUtils.hasText(major)) {
            queryWrapper.eq(StudentInfo::getMajor, major);
        }

        // 毕业年份查询
        String graduationYear = (String) params.get("graduationYear");
        if (StringUtils.hasText(graduationYear)) {
            queryWrapper.eq(StudentInfo::getGraduationYear, graduationYear);
        }

        // 统计总人数
        long totalCount = studentInfoMapper.selectCount(queryWrapper);
        result.put("totalCount", totalCount);

        // 统计已就业人数
        LambdaQueryWrapper<StudentInfo> employedWrapper = queryWrapper.clone();
        employedWrapper.eq(StudentInfo::getEmploymentStatus, 1);
        long employedCount = studentInfoMapper.selectCount(employedWrapper);
        result.put("employedCount", employedCount);

        // 统计未就业人数
        LambdaQueryWrapper<StudentInfo> unemployedWrapper = queryWrapper.clone();
        unemployedWrapper.eq(StudentInfo::getEmploymentStatus, 0);
        long unemployedCount = studentInfoMapper.selectCount(unemployedWrapper);
        result.put("unemployedCount", unemployedCount);

        // 计算就业率
        double employmentRate = totalCount > 0 ? (double) employedCount / totalCount * 100 : 0;
        result.put("employmentRate", String.format("%.2f%%", employmentRate));

        // 按专业统计就业率
        if (!StringUtils.hasText(major)) {
            List<Map<String, Object>> majorStats = getMajorStatistics(queryWrapper.clone());
            result.put("majorStatistics", majorStats);
        }

        // 按学院统计就业率
        if (!StringUtils.hasText(college)) {
            List<Map<String, Object>> collegeStats = getCollegeStatistics(queryWrapper.clone());
            result.put("collegeStatistics", collegeStats);
        }

        return result;
    }

    /**
     * 按专业统计就业信息
     */
    private List<Map<String, Object>> getMajorStatistics(LambdaQueryWrapper<StudentInfo> baseWrapper) {
        // 先查询所有专业
        List<Object> majors = studentInfoMapper.selectObjs(
                new LambdaQueryWrapper<StudentInfo>()
                        .select(StudentInfo::getMajor)
                        .groupBy(StudentInfo::getMajor));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object majorObj : majors) {
            String major = (String) majorObj;
            if (!StringUtils.hasText(major))
                continue;

            Map<String, Object> majorStat = new HashMap<>();
            majorStat.put("major", major);

            // 该专业总人数
            LambdaQueryWrapper<StudentInfo> majorWrapper = baseWrapper.clone();
            majorWrapper.eq(StudentInfo::getMajor, major);
            long totalCount = studentInfoMapper.selectCount(majorWrapper);
            majorStat.put("totalCount", totalCount);

            // 该专业已就业人数
            LambdaQueryWrapper<StudentInfo> employedWrapper = majorWrapper.clone();
            employedWrapper.eq(StudentInfo::getEmploymentStatus, 1);
            long employedCount = studentInfoMapper.selectCount(employedWrapper);
            majorStat.put("employedCount", employedCount);

            // 计算就业率
            double employmentRate = totalCount > 0 ? (double) employedCount / totalCount * 100 : 0;
            majorStat.put("employmentRate", String.format("%.2f%%", employmentRate));

            result.add(majorStat);
        }

        return result;
    }

    /**
     * 按学院统计就业信息
     */
    private List<Map<String, Object>> getCollegeStatistics(LambdaQueryWrapper<StudentInfo> baseWrapper) {
        // 先查询所有学院
        List<Object> colleges = studentInfoMapper.selectObjs(
                new LambdaQueryWrapper<StudentInfo>()
                        .select(StudentInfo::getCollege)
                        .groupBy(StudentInfo::getCollege));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object collegeObj : colleges) {
            String college = (String) collegeObj;
            if (!StringUtils.hasText(college))
                continue;

            Map<String, Object> collegeStat = new HashMap<>();
            collegeStat.put("college", college);

            // 该学院总人数
            LambdaQueryWrapper<StudentInfo> collegeWrapper = baseWrapper.clone();
            collegeWrapper.eq(StudentInfo::getCollege, college);
            long totalCount = studentInfoMapper.selectCount(collegeWrapper);
            collegeStat.put("totalCount", totalCount);

            // 该学院已就业人数
            LambdaQueryWrapper<StudentInfo> employedWrapper = collegeWrapper.clone();
            employedWrapper.eq(StudentInfo::getEmploymentStatus, 1);
            long employedCount = studentInfoMapper.selectCount(employedWrapper);
            collegeStat.put("employedCount", employedCount);

            // 计算就业率
            double employmentRate = totalCount > 0 ? (double) employedCount / totalCount * 100 : 0;
            collegeStat.put("employmentRate", String.format("%.2f%%", employmentRate));

            result.add(collegeStat);
        }

        return result;
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件路径
     */
    private String uploadFile(MultipartFile file) {
        try {
            // 生成文件保存路径
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException("文件名不能为空");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String subDir = "student/resume";
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
}