package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fq.yznu.eaps.entity.SystemConfig;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param userId      修改人ID
     * @return 是否成功
     */
    boolean updateConfigValue(String configKey, String configValue, Long userId);

    /**
     * 获取配置值并转换为指定类型
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @return 转换后的配置值
     */
    <T> T getConfigObject(String configKey, Class<T> clazz);
}