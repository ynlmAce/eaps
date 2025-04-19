package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.StudentInfo;
import com.fq.yznu.eaps.service.StudentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Map;

/**
 * 学生信息控制器
 */
@RestController
@RequestMapping("/student")
public class StudentInfoController {

    @Autowired
    private StudentInfoService studentInfoService;

    /**
     * 获取当前学生信息（学生权限）
     *
     * @return 学生信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Map<String, Object>> getCurrentStudentInfo() {
        Map<String, Object> result = studentInfoService.getCurrentStudentInfo();
        return ResponseResult.success(result);
    }

    /**
     * 更新学生信息（学生权限）
     *
     * @param studentInfo 学生信息
     * @param resume      简历文件（可选）
     * @return 成功响应
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> updateStudentInfo(
            @RequestPart("studentInfo") @Valid StudentInfo studentInfo,
            @RequestPart(value = "resume", required = false) MultipartFile resume) {
        studentInfoService.updateStudentInfo(studentInfo, resume);
        return ResponseResult.success();
    }

    /**
     * 获取学生列表（管理员权限）
     *
     * @param params 查询参数
     * @return 学生列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getStudentList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = studentInfoService.getStudentList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取学生详情（学生、辅导员、管理员权限）
     *
     * @param id 学生ID
     * @return 学生详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_COUNSELOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getStudentDetail(@PathVariable Long id) {
        Map<String, Object> result = studentInfoService.getStudentDetail(id);
        return ResponseResult.success(result);
    }

    /**
     * 获取辅导员的学生列表（辅导员权限）
     *
     * @param params 查询参数
     * @return 学生列表
     */
    @GetMapping("/counselor")
    @PreAuthorize("hasRole('ROLE_COUNSELOR')")
    public ResponseResult<Map<String, Object>> getCounselorStudentList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = studentInfoService.getCounselorStudentList(params);
        return ResponseResult.success(result);
    }

    /**
     * 更新学生信息（管理员权限）
     *
     * @param id          学生ID
     * @param studentInfo 学生信息
     * @param resume      简历文件（可选）
     * @return 成功响应
     */
    @PostMapping("/admin/update/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> updateStudentInfoByAdmin(
            @PathVariable Long id,
            @RequestPart("studentInfo") @Valid StudentInfo studentInfo,
            @RequestPart(value = "resume", required = false) MultipartFile resume) {
        studentInfo.setId(id);
        studentInfoService.updateStudentInfo(studentInfo, resume);
        return ResponseResult.success();
    }

    /**
     * 更新学生就业状态（学生、辅导员、管理员权限）
     *
     * @param id               学生ID
     * @param employmentStatus 就业状态
     * @return 成功响应
     */
    @PutMapping("/{id}/employment-status")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_COUNSELOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> updateEmploymentStatus(
            @PathVariable Long id,
            @RequestParam Integer employmentStatus) {
        studentInfoService.updateEmploymentStatus(id, employmentStatus);
        return ResponseResult.success();
    }

    /**
     * 获取就业统计信息（辅导员、管理员权限）
     *
     * @param params 查询参数
     * @return 就业统计信息
     */
    @GetMapping("/statistics/employment")
    @PreAuthorize("hasAnyRole('ROLE_COUNSELOR', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getEmploymentStatistics(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = studentInfoService.getEmploymentStatistics(params);
        return ResponseResult.success(result);
    }
} 