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
@TableName("system_config")
public class SystemConfig extends BaseEntity {

    /**
     * 配置键名
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置类型（string,number,boolean,json）
     */
    private String configType;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifiedTime;

    /**
     * 修改人ID
     */
    private Long modifiedByUserId;
}