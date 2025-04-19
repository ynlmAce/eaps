package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.JobPosition;
import com.fq.yznu.eaps.service.JobPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 职位控制器
 */
@RestController
@RequestMapping("/job/position")
public class JobPositionController {

    @Autowired
    private JobPositionService jobPositionService;

    /**
     * 获取职位列表
     *
     * @param params 查询参数
     * @return 职位列表
     */
    @GetMapping("/list")
    public ResponseResult<Map<String, Object>> getPositionList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = jobPositionService.getPositionList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取职位详情
     *
     * @param id 职位ID
     * @return 职位详情
     */
    @GetMapping("/{id}")
    public ResponseResult<JobPosition> getPositionDetail(@PathVariable Long id) {
        JobPosition result = jobPositionService.getPositionDetail(id);
        return ResponseResult.success(result);
    }

    /**
     * 发布职位（企业权限）
     *
     * @param jobPosition 职位信息
     * @return 成功响应
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> publishPosition(@RequestBody @Valid JobPosition jobPosition) {
        jobPositionService.publishPosition(jobPosition);
        return ResponseResult.success();
    }

    /**
     * 更新职位（企业权限）
     *
     * @param id          职位ID
     * @param jobPosition 职位信息
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> updatePosition(@PathVariable Long id, @RequestBody @Valid JobPosition jobPosition) {
        jobPosition.setId(id);
        jobPositionService.updatePosition(jobPosition);
        return ResponseResult.success();
    }

    /**
     * 删除职位（企业权限）
     *
     * @param id 职位ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> deletePosition(@PathVariable Long id) {
        jobPositionService.deletePosition(id);
        return ResponseResult.success();
    }

    /**
     * 审核职位（管理员权限）
     *
     * @param id           职位ID
     * @param auditParam 审核参数
     * @return 成功响应
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> auditPosition(@PathVariable Long id, @RequestBody Map<String, Object> auditParam) {
        Integer status = (Integer) auditParam.get("status");
        String reason = (String) auditParam.get("reason");
        jobPositionService.auditPosition(id, status, reason);
        return ResponseResult.success();
    }

    /**
     * 获取企业的职位列表（企业权限）
     *
     * @param params 查询参数
     * @return 职位列表
     */
    @GetMapping("/enterprise")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Map<String, Object>> getEnterprisePositionList(@RequestParam Map<String, Object> params) {
        // 企业只能查看自己的职位，在service层会自动过滤
        Map<String, Object> result = jobPositionService.getPositionList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取待审核的职位列表（管理员权限）
     *
     * @param params 查询参数
     * @return 职位列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getPendingPositionList(@RequestParam Map<String, Object> params) {
        params.put("status", 0); // 0表示待审核
        Map<String, Object> result = jobPositionService.getPositionList(params);
        return ResponseResult.success(result);
    }
} 