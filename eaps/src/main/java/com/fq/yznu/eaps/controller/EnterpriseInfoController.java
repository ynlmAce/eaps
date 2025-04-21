package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.service.EnterpriseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 企业信息控制器
 */
@RestController
@RequestMapping("/enterprise")
public class EnterpriseInfoController {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    /**
     * 获取当前企业信息（企业权限）
     *
     * @return 企业信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<EnterpriseInfo> getCurrentEnterpriseInfo() {
        EnterpriseInfo result = enterpriseInfoService.getCurrentEnterpriseInfo();
        return ResponseResult.success(result);
    }

    /**
     * 更新企业信息（企业权限）
     *
     * @param enterpriseInfo 企业信息
     * @param license        营业执照文件（可选）
     * @param logo           企业Logo文件（可选）
     * @return 成功响应
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> updateEnterpriseInfo(
            @RequestPart("enterpriseInfo") @Valid EnterpriseInfo enterpriseInfo,
            @RequestPart(value = "license", required = false) MultipartFile license,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        enterpriseInfoService.updateEnterpriseInfo(enterpriseInfo, license, logo);
        return ResponseResult.success();
    }

    /**
     * 获取企业列表
     *
     * @param params 查询参数
     * @return 企业列表
     */
    @GetMapping("/list")
    public ResponseResult<Map<String, Object>> getEnterpriseList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = enterpriseInfoService.getEnterpriseList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取企业详情
     *
     * @param id 企业ID
     * @return 企业详情
     */
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> getEnterpriseDetail(@PathVariable Long id) {
        Map<String, Object> result = enterpriseInfoService.getEnterpriseDetail(id);
        return ResponseResult.success(result);
    }

    /**
     * 审核企业信息（管理员权限）
     *
     * @param id          企业ID
     * @param verifyParam 审核参数
     * @return 成功响应
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> verifyEnterprise(
            @PathVariable Long id,
            @RequestBody Map<String, Object> verifyParam) {
        Integer verifyStatus = (Integer) verifyParam.get("verifyStatus");
        String verifyComment = (String) verifyParam.get("verifyComment");
        enterpriseInfoService.verifyEnterprise(id, verifyStatus, verifyComment);
        return ResponseResult.success();
    }

    /**
     * 获取待审核的企业列表（管理员权限）
     *
     * @param params 查询参数
     * @return 企业列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getPendingEnterpriseList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = enterpriseInfoService.getPendingEnterpriseList(params);
        return ResponseResult.success(result);
    }
} 