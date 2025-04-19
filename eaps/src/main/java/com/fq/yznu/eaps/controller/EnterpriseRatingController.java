package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.EnterpriseRating;
import com.fq.yznu.eaps.service.EnterpriseRatingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 企业评分控制器
 */
@Api(tags = "企业评分接口")
@RestController
@RequestMapping("/api/rating")
@Validated
@RequiredArgsConstructor
@Slf4j
public class EnterpriseRatingController {

    private final EnterpriseRatingService enterpriseRatingService;

    /**
     * 提交企业评分
     *
     * @param rating 评分信息
     * @return 操作结果
     */
    @ApiOperation("提交企业评分")
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> submitRating(@RequestBody @Valid EnterpriseRating rating) {
        boolean result = enterpriseRatingService.submitRating(rating);
        return result ? ResponseResult.success() : ResponseResult.error("提交评分失败");
    }

    /**
     * 更新企业评分
     *
     * @param rating 评分信息
     * @return 操作结果
     */
    @ApiOperation("更新企业评分")
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
    @ApiOperation("删除企业评分")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> deleteRating(@PathVariable @NotNull(message = "评分ID不能为空") Long id) {
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
    @ApiOperation("企业回复评分")
    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> replyRating(
            @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @RequestBody @NotNull(message = "回复内容不能为空") Map<String, String> replyMap) {
        String reply = replyMap.get("reply");
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
    @ApiOperation("企业申诉评分")
    @PostMapping("/{id}/appeal")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> appealRating(
            @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @RequestBody @NotNull(message = "申诉内容不能为空") Map<String, String> appealMap) {
        String appealContent = appealMap.get("appealContent");
        boolean result = enterpriseRatingService.appealRating(id, appealContent);
        return result ? ResponseResult.success() : ResponseResult.error("申诉评分失败");
    }

    /**
     * 管理员审核评分
     *
     * @param id            评分ID
     * @param status        审核状态
     * @param reviewComment 审核意见
     * @return 操作结果
     */
    @ApiOperation("管理员审核评分")
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> reviewRating(
            @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @RequestBody Map<String, Object> reviewMap) {
        Integer status = (Integer) reviewMap.get("status");
        String reviewComment = (String) reviewMap.get("reviewComment");
        boolean result = enterpriseRatingService.reviewRating(id, status, reviewComment);
        return result ? ResponseResult.success() : ResponseResult.error("审核评分失败");
    }

    /**
     * 管理员审核申诉
     *
     * @param id                  评分ID
     * @param appealStatus        申诉处理结果
     * @param appealReviewComment 处理意见
     * @return 操作结果
     */
    @ApiOperation("管理员审核申诉")
    @PostMapping("/{id}/review-appeal")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> reviewAppeal(
            @PathVariable @NotNull(message = "评分ID不能为空") Long id,
            @RequestBody Map<String, Object> reviewMap) {
        Integer appealStatus = (Integer) reviewMap.get("appealStatus");
        String appealReviewComment = (String) reviewMap.get("appealReviewComment");
        boolean result = enterpriseRatingService.reviewAppeal(id, appealStatus, appealReviewComment);
        return result ? ResponseResult.success() : ResponseResult.error("审核申诉失败");
    }

    /**
     * 获取评分详情
     *
     * @param id 评分ID
     * @return 评分详情
     */
    @ApiOperation("获取评分详情")
    @GetMapping("/{id}")
    public ResponseResult<EnterpriseRating> getRatingDetail(@PathVariable @NotNull(message = "评分ID不能为空") Long id) {
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
    @ApiOperation("分页查询企业评分列表")
    @GetMapping("/list")
    public ResponseResult<Page<Map<String, Object>>> pageRatingList(
            @RequestParam @NotNull(message = "企业ID不能为空") Long enterpriseId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页条数最小为1") @Max(value = 100, message = "每页条数最大为100") Integer pageSize,
            @RequestParam(defaultValue = "latest") String orderBy) {
        Page<Map<String, Object>> page = enterpriseRatingService.pageRatingList(enterpriseId, pageNum, pageSize, orderBy);
        return ResponseResult.success(page);
    }

    /**
     * 获取学生的评分列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 评分列表
     */
    @ApiOperation("获取学生的评分列表")
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
    @ApiOperation("获取企业收到的评分列表")
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
    @ApiOperation("管理员获取待处理的评分列表")
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
    @ApiOperation("获取企业平均评分")
    @GetMapping("/average/{enterpriseId}")
    public ResponseResult<Map<String, Object>> getAverageRating(
            @PathVariable @NotNull(message = "企业ID不能为空") Long enterpriseId) {
        Map<String, Object> avgRating = enterpriseRatingService.getAverageRating(enterpriseId);
        return ResponseResult.success(avgRating);
    }

    /**
     * 标记评分为有用
     *
     * @param id 评分ID
     * @return 操作结果
     */
    @ApiOperation("标记评分为有用")
    @PostMapping("/{id}/helpful")
    public ResponseResult<Void> markRatingHelpful(
            @PathVariable @NotNull(message = "评分ID不能为空") Long id) {
        boolean result = enterpriseRatingService.markRatingHelpful(id);
        return result ? ResponseResult.success() : ResponseResult.error("标记失败");
    }

    /**
     * 获取企业热门评分列表
     *
     * @param enterpriseId 企业ID
     * @param limit        数量限制
     * @return 热门评分列表
     */
    @ApiOperation("获取企业热门评分列表")
    @GetMapping("/hot/{enterpriseId}")
    public ResponseResult<List<Map<String, Object>>> getHotRatingList(
            @PathVariable @NotNull(message = "企业ID不能为空") Long enterpriseId,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "数量最小为1") @Max(value = 20, message = "数量最大为20") Integer limit) {
        List<Map<String, Object>> hotList = enterpriseRatingService.getHotRatingList(enterpriseId, limit);
        return ResponseResult.success(hotList);
    }
}