package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.JobApplication;
import com.fq.yznu.eaps.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 职位申请控制器
 */
@RestController
@RequestMapping("/job/application")
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;

    /**
     * 提交职位申请（学生权限）
     *
     * @param application 申请信息
     * @param resume      简历文件（可选）
     * @return 成功响应
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> submitApplication(
            @RequestPart("application") @Valid JobApplication application,
            @RequestPart(value = "resume", required = false) MultipartFile resume) {
        jobApplicationService.submitApplication(application, resume);
        return ResponseResult.success();
    }

    /**
     * 获取我的申请列表（学生权限）
     *
     * @param params 查询参数
     * @return 申请列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Map<String, Object>> getMyApplicationList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = jobApplicationService.getMyApplicationList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取申请详情（学生和企业权限）
     *
     * @param id 申请ID
     * @return 申请详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ENTERPRISE', 'ROLE_ADMIN')")
    public ResponseResult<Map<String, Object>> getApplicationDetail(@PathVariable Long id) {
        Map<String, Object> result = jobApplicationService.getApplicationDetail(id);
        return ResponseResult.success(result);
    }

    /**
     * 撤销申请（学生权限）
     *
     * @param id 申请ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> cancelApplication(@PathVariable Long id) {
        jobApplicationService.cancelApplication(id);
        return ResponseResult.success();
    }

    /**
     * 获取企业的申请列表（企业权限）
     *
     * @param params 查询参数
     * @return 申请列表
     */
    @GetMapping("/enterprise")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Map<String, Object>> getEnterpriseApplicationList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = jobApplicationService.getEnterpriseApplicationList(params);
        return ResponseResult.success(result);
    }

    /**
     * 更新申请状态（企业权限）
     *
     * @param id           申请ID
     * @param statusUpdate 状态更新信息
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> statusUpdate) {
        Integer status = (Integer) statusUpdate.get("status");
        String feedback = (String) statusUpdate.get("feedback");
        jobApplicationService.updateApplicationStatus(id, status, feedback, statusUpdate);
        return ResponseResult.success();
    }

    /**
     * 获取某个职位的申请列表（企业权限）
     *
     * @param jobId  职位ID
     * @param params 查询参数
     * @return 申请列表
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Map<String, Object>> getJobApplicationList(
            @PathVariable Long jobId,
            @RequestParam Map<String, Object> params) {
        params.put("jobId", jobId);
        Map<String, Object> result = jobApplicationService.getEnterpriseApplicationList(params);
        return ResponseResult.success(result);
    }
} 