package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.Result;
import com.fq.yznu.eaps.entity.SystemConfig;
import com.fq.yznu.eaps.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取配置列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Page<SystemConfig>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) String configType) {

        Page<SystemConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(configKey), SystemConfig::getConfigKey, configKey)
                .eq(StringUtils.hasText(configType), SystemConfig::getConfigType, configType)
                .orderByDesc(SystemConfig::getCreateTime);

        return Result.success(systemConfigService.page(page, wrapper));
    }

    @Operation(summary = "获取配置详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<SystemConfig> getInfo(@PathVariable Long id) {
        return Result.success(systemConfigService.getById(id));
    }

    @Operation(summary = "根据键名获取配置")
    @GetMapping("/key/{configKey}")
    public Result<String> getConfigValue(@PathVariable String configKey) {
        return Result.success(systemConfigService.getConfigValue(configKey));
    }

    @Operation(summary = "新增配置")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Boolean> add(@RequestBody SystemConfig config) {
        config.setCreateTime(LocalDateTime.now());
        config.setLastModifiedTime(LocalDateTime.now());
        return Result.success(systemConfigService.save(config));
    }

    @Operation(summary = "修改配置")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Boolean> update(@RequestBody SystemConfig config) {
        config.setLastModifiedTime(LocalDateTime.now());
        return Result.success(systemConfigService.updateById(config));
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(systemConfigService.removeById(id));
    }
}