package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SystemConfig extends BaseEntity {
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 系统内置（0否 1是）
     */
    private Integer configType;
    
    /**
     * 配置分组
     */
    private String configGroup;
    
    /**
     * 是否允许修改（0允许 1不允许）
     */
    private Integer isModifiable;
    
    /**
     * 配置状态（0正常 1停用）
     */
    private Integer status;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 配置值类型（0字符串 1数字 2布尔 3JSON）
     */
    private Integer valueType;
    
    /**
     * 修改人
     */
    private String modifier;
    
    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;
} 