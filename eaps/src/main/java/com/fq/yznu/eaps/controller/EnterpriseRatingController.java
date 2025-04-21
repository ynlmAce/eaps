package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.EnterpriseRating;
import com.fq.yznu.eaps.service.EnterpriseRatingService;
import com.fq.yznu.eaps.util.SecurityUtils;
import com.fq.yznu.eaps.dto.EnterpriseRatingDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 企业评分控制器
 */
@Tag(name = "企业评价管理", description = "企业评价相关接口")
@RestController
@RequestMapping("/enterprise/rating")
@Validated
@RequiredArgsConstructor
@Slf4j
public class EnterpriseRatingController {

    private final EnterpriseRatingService enterpriseRatingService;

    /**
     * 提交企业评分
     *
     * @param ratingDTO 评分信息
     * @return 操作结果
     */
    @Operation(summary = "提交企业评价")
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseResult<Void> submitRating(
            @Parameter(description = "评价信息") @RequestBody @Valid EnterpriseRatingDTO ratingDTO) {
        enterpriseRatingService.submitRating(ratingDTO);
        return ResponseResult.success();
    }

    /**
     * 更新企业评分
     *
     * @param rating 评分信息
     * @return 操作结果
     */
    @Operation(summary = "更新企业评分")
    @PutMapping("/update")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> updateRating(@RequestBody @Valid EnterpriseRating rating) {
        boolean result = enterpriseRatingService.updateRating(rating);
        return result ? ResponseResult.success() : ResponseResult.error("更新评分失败");
    }

    /**
     * 删除企业评分
     *
     * @param id 评分ID
     * @return 操作结果
     */
    @Operation(summary = "删除企业评分")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> deleteRating(
            @Parameter(description = "评分ID") @PathVariable @NotNull(message = "评分ID不能为空") Long id) {
        boolean result = enterpriseRatingService.deleteRating(id);
        return result ? ResponseResult.success() : ResponseResult.error("删除评分失败");
    }

    /**
     * 企业回复评分
     *
     * @param id    评分ID
     * @param reply 回复内容
     * @return 操作结果
     */
    @Operation(summary = "回复企业评价")
    @PostMapping("/reply/{id}")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> replyRating(
            @Parameter(description = "评价ID") @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @Parameter(description = "回复内容") @RequestParam String reply) {
        boolean result = enterpriseRatingService.replyRating(id, reply);
        return result ? ResponseResult.success() : ResponseResult.error("回复评分失败");
    }

    /**
     * 企业申诉评分
     *
     * @param id            评分ID
     * @param appealContent 申诉内容
     * @return 操作结果
     */
    @Operation(summary = "提交申诉")
    @PostMapping("/appeal/{id}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ResponseResult<Void> submitAppeal(
            @Parameter(description = "评价ID") @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @Parameter(description = "申诉原因") @RequestParam String reason,
            @Parameter(description = "申诉证据") @RequestParam(required = false) String evidence) {
        boolean result = enterpriseRatingService.submitAppeal(id, reason, evidence);
        return result ? ResponseResult.success() : ResponseResult.error("提交申诉失败");
    }

    /**
     * 管理员审核评分
     *
     * @param id            评分ID
     * @param status        审核状态
     * @param reviewComment 审核意见
     * @return 操作结果
     */
    @Operation(summary = "审核申诉")
    @PostMapping("/appeal/review/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseResult<Void> reviewAppeal(
            @Parameter(description = "评价ID") @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @Parameter(description = "审核结果") @RequestParam Integer status,
            @Parameter(description = "审核备注") @RequestParam(required = false) String remark) {
        boolean result = enterpriseRatingService.reviewAppeal(id, status, remark);
        return result ? ResponseResult.success() : ResponseResult.error("审核申诉失败");
    }

    /**
     * 获取评分详情
     *
     * @param id 评分ID
     * @return 评分详情
     */
    @Operation(summary = "获取评价详情")
    @GetMapping("/{id}")
    public ResponseResult<EnterpriseRating> getRatingDetail(
            @Parameter(description = "评价ID") @PathVariable @NotNull(message = "评分ID不能为空") Long id) {
        EnterpriseRating rating = enterpriseRatingService.getRatingDetail(id);
        return ResponseResult.success(rating);
    }

    /**
     * 分页查询企业评分列表
     *
     * @param enterpriseId 企业ID
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param orderBy      排序方式
     * @return 评分列表
     */
    @Operation(summary = "获取企业评价列表")
    @GetMapping("/list")
    public ResponseResult<Object> getRatingList(
            @Parameter(description = "企业ID") @RequestParam(required = false) Long enterpriseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return ResponseResult.success(enterpriseRatingService.getRatingList(enterpriseId, pageNum, pageSize));
    }

    /**
     * 获取学生的评分列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 评分列表
     */
    @Operation(summary = "获取学生的评分列表")
    @GetMapping("/student")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Page<Map<String, Object>>> getStudentRatingList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer pageSize) {
        Page<Map<String, Object>> page = enterpriseRatingService.getStudentRatingList(pageNum, pageSize);
        return ResponseResult.success(page);
    }

    /**
     * 获取企业收到的评分列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 评分列表
     */
    @Operation(summary = "获取企业收到的评分列表")
    @GetMapping("/enterprise")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Page<Map<String, Object>>> getEnterpriseRatingList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer pageSize) {
        Page<Map<String, Object>> page = enterpriseRatingService.getEnterpriseRatingList(pageNum, pageSize);
        return ResponseResult.success(page);
    }

    /**
     * 管理员获取待处理的评分列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param type     查询类型
     * @return 评分列表
     */
    @Operation(summary = "管理员获取待处理的评分列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Page<Map<String, Object>>> getPendingRatingList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer pageSize,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "类型最小为1") @Max(value = 2, message = "类型最大为2") Integer type) {
        Page<Map<String, Object>> page = enterpriseRatingService.getPendingRatingList(pageNum, pageSize, type);
        return ResponseResult.success(page);
    }

    /**
     * 获取企业平均评分
     *
     * @param enterpriseId 企业ID
     * @return 平均评分信息
     */
    @Operation(summary = "获取企业平均评分")
    @GetMapping("/average/{enterpriseId}")
    public ResponseResult<Map<String, Object>> getAverageRating(
            @PathVariable @NotNull(message = "企业ID不能为空") Long enterpriseId) {
        Map<String, Object> avgRating = enterpriseRatingService.getEnterpriseAverageRating(enterpriseId);
        return ResponseResult.success(avgRating);
    }

    /**
     * 标记评分为有用
     *
     * @param id 评分ID
     * @return 操作结果
     */
    @Operation(summary = "标记评分为有用")
    @PostMapping("/{id}/helpful")
    public ResponseResult<Void> markRatingHelpful(
            @PathVariable @NotNull(message = "评分ID不能为空") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean result = enterpriseRatingService.markRatingHelpful(id, userId);
        return result ? ResponseResult.success() : ResponseResult.error("标记失败");
    }

    /**
     * 获取企业热门评分列表
     *
     * @param enterpriseId 企业ID
     * @param limit        数量限制
     * @return 热门评分列表
     */
    @Operation(summary = "获取企业热门评分列表")
    @GetMapping("/hot/{enterpriseId}")
    public ResponseResult<List<Map<String, Object>>> getHotRatingList(
            @PathVariable @NotNull(message = "企业ID不能为空") Long enterpriseId,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "数量最小为1") @Max(value = 20, message = "数量最大为20") Integer limit) {
        List<Map<String, Object>> hotList = enterpriseRatingService.getHotRatingList(enterpriseId, limit);
        return ResponseResult.success(hotList);
    }
}