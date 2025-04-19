package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.EnterprisePromotion;
import com.fq.yznu.eaps.service.EnterprisePromotionService;
import com.fq.yznu.eaps.service.FileStorageService;
import com.fq.yznu.eaps.vo.request.PromotionQueryReq;
import com.fq.yznu.eaps.vo.request.PromotionReviewReq;
import com.fq.yznu.eaps.vo.response.PromotionDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业推广控制器
 */
@Slf4j
@Api(tags = "企业推广管理")
@RestController
@RequestMapping("/enterprise/promotion")
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EnterprisePromotionController {

    private final EnterprisePromotionService enterprisePromotionService;
    private final FileStorageService fileStorageService;

    /**
     * 获取企业推广列表（分页）
     *
     * @param queryReq 查询参数
     * @return 企业推广列表
     */
    @ApiOperation("获取企业推广列表")
    @GetMapping("/list")
    public ResponseResult<Map<String, Object>> getPromotionList(
            @Validated PromotionQueryReq queryReq) {
        log.info("获取企业推广列表, 参数: {}", queryReq);
        
        // 将DTO转为Map参数
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", queryReq.getPageNum());
        params.put("pageSize", queryReq.getPageSize());
        params.put("enterpriseId", queryReq.getEnterpriseId());
        params.put("keyword", queryReq.getKeyword());
        params.put("type", queryReq.getType());
        params.put("status", queryReq.getStatus());
        
        // 使用Map参数调用service
        Map<String, Object> result = enterprisePromotionService.pageList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取企业推广详情
     *
     * @param id 推广ID
     * @return 企业推广详情
     */
    @ApiOperation("获取企业推广详情")
    @GetMapping("/{id}")
    public ResponseResult<EnterprisePromotion> getPromotionDetail(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id) {
        log.info("获取企业推广详情, id: {}", id);
        
        EnterprisePromotion result = enterprisePromotionService.getPromotionDetail(id);
        if (result == null) {
            return ResponseResult.error("推广信息不存在");
        }
        
        // 增加浏览次数
        enterprisePromotionService.incrementViewCount(id);
        return ResponseResult.success(result);
    }

    /**
     * 保存企业推广（企业权限）
     *
     * @param promotion 企业推广信息
     * @return 成功响应
     */
    @ApiOperation("保存企业推广")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> savePromotion(@RequestBody @Valid EnterprisePromotion promotion) {
        log.info("保存企业推广, 标题: {}", promotion.getTitle());
        
        // 设置当前登录企业ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.parseLong(authentication.getName());
        promotion.setCreatorId(currentUserId);
        promotion.setStatus(0); // 默认待审核状态
        
        boolean success = enterprisePromotionService.savePromotion(promotion);
        if (!success) {
            return ResponseResult.error("保存失败");
        }
        
        return ResponseResult.success();
    }

    /**
     * 更新企业推广（企业权限）
     *
     * @param id         推广ID
     * @param promotion  企业推广信息
     * @return 成功响应
     */
    @ApiOperation("更新企业推广")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> updatePromotion(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id, 
            @RequestBody @Valid EnterprisePromotion promotion) {
        log.info("更新企业推广, id: {}, 标题: {}", id, promotion.getTitle());
        
        promotion.setId(id);
        // 设置为待审核状态
        promotion.setStatus(0);
        
        boolean success = enterprisePromotionService.updatePromotion(promotion);
        if (!success) {
            return ResponseResult.error("更新失败");
        }
        
        return ResponseResult.success();
    }

    /**
     * 删除企业推广（企业权限）
     *
     * @param id 推广ID
     * @return 成功响应
     */
    @ApiOperation("删除企业推广")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> deletePromotion(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id) {
        log.info("删除企业推广, id: {}", id);
        
        boolean success = enterprisePromotionService.removePromotion(id);
        if (!success) {
            return ResponseResult.error("删除失败");
        }
        
        return ResponseResult.success();
    }

    /**
     * 审核企业推广（管理员权限）
     *
     * @param id         推广ID
     * @param reviewReq  审核参数
     * @return 成功响应
     */
    @ApiOperation("审核企业推广")
    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> reviewPromotion(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id, 
            @RequestBody @Valid PromotionReviewReq reviewReq) {
        log.info("审核企业推广, id: {}, 状态: {}", id, reviewReq.getStatus());
        
        reviewReq.setId(id);
        
        // 获取当前审核人ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long reviewerId = Long.parseLong(authentication.getName());
        
        boolean success = enterprisePromotionService.review(
            id, 
            reviewReq.getStatus(), 
            reviewReq.getReviewRemark(), 
            reviewerId);
            
        if (!success) {
            return ResponseResult.error("审核失败");
        }
        
        return ResponseResult.success();
    }

    /**
     * 设置推广置顶状态（管理员权限）
     *
     * @param id       推广ID
     * @param isTop    是否置顶
     * @return 成功响应
     */
    @ApiOperation("设置推广置顶状态")
    @PutMapping("/{id}/top")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> setPromotionTopStatus(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id, 
            @ApiParam(value = "是否置顶", required = true) 
            @RequestParam @NotNull(message = "置顶状态不能为空") Boolean isTop) {
        log.info("设置推广置顶状态, id: {}, isTop: {}", id, isTop);
        
        boolean success = enterprisePromotionService.setTopStatus(id, isTop ? 1 : 0);
        if (!success) {
            return ResponseResult.error("设置置顶状态失败");
        }
        
        return ResponseResult.success();
    }

    /**
     * 获取企业自己的推广列表（企业权限）
     *
     * @param queryReq 查询参数
     * @return 推广列表
     */
    @ApiOperation("获取企业自己的推广列表")
    @GetMapping("/enterprise")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Map<String, Object>> getEnterprisePromotionList(
            @Validated PromotionQueryReq queryReq) {
        log.info("获取企业自己的推广列表, 参数: {}", queryReq);
        
        // 获取当前企业ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long enterpriseId = Long.parseLong(authentication.getName());
        
        // 将DTO转为Map参数
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", queryReq.getPageNum());
        params.put("pageSize", queryReq.getPageSize());
        // 强制设置为当前企业ID
        params.put("enterpriseId", enterpriseId);
        params.put("keyword", queryReq.getKeyword());
        params.put("type", queryReq.getType());
        params.put("status", queryReq.getStatus());
        
        // 使用Map参数调用service
        Map<String, Object> result = enterprisePromotionService.pageList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取待审核的推广列表（管理员权限）
     *
     * @param queryReq 查询参数
     * @return 推广列表
     */
    @ApiOperation("获取待审核的推广列表")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getPendingPromotionList(
            @Validated PromotionQueryReq queryReq) {
        log.info("获取待审核的推广列表, 参数: {}", queryReq);
        
        // 将DTO转为Map参数
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", queryReq.getPageNum());
        params.put("pageSize", queryReq.getPageSize());
        params.put("enterpriseId", queryReq.getEnterpriseId());
        params.put("keyword", queryReq.getKeyword());
        params.put("type", queryReq.getType());
        // 强制设置状态为待审核
        params.put("status", 0);
        
        // 使用Map参数调用service
        Map<String, Object> result = enterprisePromotionService.pageList(params);
        return ResponseResult.success(result);
    }

    /**
     * 获取最新推广列表
     *
     * @param type  推广类型
     * @param limit 限制数量
     * @return 推广列表
     */
    @ApiOperation("获取最新推广列表")
    @GetMapping("/latest")
    public ResponseResult<List<EnterprisePromotion>> getLatestPromotionList(
            @ApiParam(value = "推广类型") @RequestParam(required = false) Integer type,
            @ApiParam(value = "限制数量") @RequestParam(defaultValue = "5") 
            @Min(value = 1, message = "数量最小为1") 
            @Max(value = 50, message = "数量最大为50") Integer limit) {
        log.info("获取最新推广列表, 类型: {}, 数量: {}", type, limit);
        
        List<EnterprisePromotion> result = enterprisePromotionService.getLatestPromotions(type, limit);
        return ResponseResult.success(result);
    }

    /**
     * 获取热门推广列表
     *
     * @param type  推广类型
     * @param limit 限制数量
     * @return 推广列表
     */
    @ApiOperation("获取热门推广列表")
    @GetMapping("/hot")
    public ResponseResult<List<EnterprisePromotion>> getHotPromotionList(
            @ApiParam(value = "推广类型") @RequestParam(required = false) Integer type,
            @ApiParam(value = "限制数量") @RequestParam(defaultValue = "5") 
            @Min(value = 1, message = "数量最小为1") 
            @Max(value = 50, message = "数量最大为50") Integer limit) {
        log.info("获取热门推广列表, 类型: {}, 数量: {}", type, limit);
        
        List<EnterprisePromotion> result = enterprisePromotionService.getHotPromotions(type, limit);
        return ResponseResult.success(result);
    }

    /**
     * 批量删除企业推广（企业权限）
     *
     * @param ids 推广ID列表
     * @return 成功响应
     */
    @ApiOperation("批量删除企业推广")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> batchDeletePromotion(
            @ApiParam(value = "推广ID列表", required = true) 
            @RequestBody @NotNull(message = "推广ID列表不能为空") List<Long> ids) {
        log.info("批量删除企业推广, ids: {}", ids);
        
        if (ids.isEmpty()) {
            return ResponseResult.validateFailed("推广ID列表不能为空");
        }
        
        boolean success = enterprisePromotionService.batchRemovePromotion(ids);
        if (!success) {
            return ResponseResult.error("批量删除失败");
        }
        
        return ResponseResult.success();
    }
    
    /**
     * 获取待审核的推广数量（管理员权限）
     *
     * @return 待审核数量
     */
    @ApiOperation("获取待审核的推广数量")
    @GetMapping("/pending/count")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Integer> getPendingPromotionCount() {
        log.info("获取待审核的推广数量");
        
        Integer count = enterprisePromotionService.countPendingReview();
        return ResponseResult.success(count);
    }

    /**
     * 保存企业推广（带文件上传）
     *
     * @param promotion     企业推广信息(JSON)
     * @param coverImage    封面图片
     * @param attachments   附件文件数组
     * @return 成功响应
     */
    @ApiOperation("保存企业推广（带文件上传）")
    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> savePromotionWithFiles(
            @ApiParam(value = "企业推广信息", required = true)
            @RequestPart("promotion") @Valid EnterprisePromotion promotion,
            @ApiParam(value = "封面图片")
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @ApiParam(value = "附件文件")
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        
        log.info("保存企业推广(带文件), 标题: {}", promotion.getTitle());
        
        try {
            // 设置当前登录企业ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = Long.parseLong(authentication.getName());
            promotion.setCreatorId(currentUserId);
            promotion.setStatus(0); // 默认待审核状态
            
            // 处理封面图片
            if (coverImage != null && !coverImage.isEmpty()) {
                String coverUrl = fileStorageService.storeFile(coverImage, "promotion/cover");
                promotion.setCoverUrl(coverUrl);
            }
            
            // 处理附件
            if (attachments != null && attachments.length > 0) {
                List<String> attachmentUrls = new ArrayList<>();
                
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        String attachmentUrl = fileStorageService.storeFile(attachment, "promotion/attachment");
                        attachmentUrls.add(attachmentUrl);
                    }
                }
                
                // 将附件URL列表转为逗号分隔的字符串
                if (!attachmentUrls.isEmpty()) {
                    promotion.setAttachmentUrls(String.join(",", attachmentUrls));
                }
            }
            
            // 保存推广信息
            boolean success = enterprisePromotionService.savePromotion(promotion);
            if (!success) {
                return ResponseResult.error("保存失败");
            }
            
            return ResponseResult.success();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseResult.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新企业推广（带文件上传）
     *
     * @param id            推广ID
     * @param promotion     企业推广信息(JSON)
     * @param coverImage    封面图片
     * @param attachments   附件文件数组
     * @return 成功响应
     */
    @ApiOperation("更新企业推广（带文件上传）")
    @PutMapping(value = "/{id}/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ENTERPRISE')")
    public ResponseResult<Void> updatePromotionWithFiles(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id,
            @ApiParam(value = "企业推广信息", required = true)
            @RequestPart("promotion") @Valid EnterprisePromotion promotion,
            @ApiParam(value = "封面图片")
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @ApiParam(value = "附件文件")
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments,
            @ApiParam(value = "删除的附件URL")
            @RequestParam(value = "deletedAttachments", required = false) List<String> deletedAttachments) {
        
        log.info("更新企业推广(带文件), id: {}, 标题: {}", id, promotion.getTitle());
        
        try {
            // 设置ID
            promotion.setId(id);
            
            // 获取原始记录
            EnterprisePromotion existingPromotion = enterprisePromotionService.getPromotionDetail(id);
            if (existingPromotion == null) {
                return ResponseResult.error("推广信息不存在");
            }
            
            // 处理封面图片
            if (coverImage != null && !coverImage.isEmpty()) {
                // 删除旧封面
                if (StringUtils.hasText(existingPromotion.getCoverUrl())) {
                    fileStorageService.deleteFile(existingPromotion.getCoverUrl());
                }
                
                // 上传新封面
                String coverUrl = fileStorageService.storeFile(coverImage, "promotion/cover");
                promotion.setCoverUrl(coverUrl);
            } else {
                // 保留原来的封面URL
                promotion.setCoverUrl(existingPromotion.getCoverUrl());
            }
            
            // 处理附件
            List<String> currentAttachmentUrls = new ArrayList<>();
            
            // 保留未删除的原有附件
            if (StringUtils.hasText(existingPromotion.getAttachmentUrls())) {
                List<String> existingAttachments = List.of(existingPromotion.getAttachmentUrls().split(","));
                
                // 过滤出未被删除的附件
                currentAttachmentUrls = existingAttachments.stream()
                        .filter(url -> deletedAttachments == null || !deletedAttachments.contains(url))
                        .collect(Collectors.toList());
                
                // 删除标记为删除的附件
                if (deletedAttachments != null) {
                    for (String urlToDelete : deletedAttachments) {
                        fileStorageService.deleteFile(urlToDelete);
                    }
                }
            }
            
            // 添加新上传的附件
            if (attachments != null && attachments.length > 0) {
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        String attachmentUrl = fileStorageService.storeFile(attachment, "promotion/attachment");
                        currentAttachmentUrls.add(attachmentUrl);
                    }
                }
            }
            
            // 更新附件URL列表
            if (!currentAttachmentUrls.isEmpty()) {
                promotion.setAttachmentUrls(String.join(",", currentAttachmentUrls));
            } else {
                promotion.setAttachmentUrls(null);
            }
            
            // 更新推广信息
            boolean success = enterprisePromotionService.updatePromotion(promotion);
            if (!success) {
                return ResponseResult.error("更新失败");
            }
            
            return ResponseResult.success();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取企业推广详情（包含企业信息）
     *
     * @param id 推广ID
     * @return 企业推广详情
     */
    @ApiOperation("获取企业推广详情（包含企业信息）")
    @GetMapping("/{id}/detail")
    public ResponseResult<PromotionDetailVO> getPromotionDetailWithEnterpriseInfo(
            @ApiParam(value = "推广ID", required = true) 
            @PathVariable @NotNull(message = "推广ID不能为空") Long id) {
        log.info("获取企业推广详情(包含企业信息), id: {}", id);
        
        PromotionDetailVO result = enterprisePromotionService.getPromotionDetailWithEnterpriseInfo(id);
        if (result == null) {
            return ResponseResult.error("推广信息不存在");
        }
        
        // 增加浏览次数
        enterprisePromotionService.incrementViewCount(id);
        return ResponseResult.success(result);
    }
} 