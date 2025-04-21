package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fq.yznu.eaps.entity.SystemConfig;
import com.fq.yznu.eaps.mapper.SystemConfigMapper;
import com.fq.yznu.eaps.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getConfigValue(String configKey) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigValue(String configKey, String configValue, Long userId) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey));
        if (config == null) {
            return false;
        }

        config.setConfigValue(configValue);
        config.setLastModifiedTime(LocalDateTime.now());
        config.setModifiedByUserId(userId);

        return updateById(config);
    }

    @Override
    public <T> T getConfigObject(String configKey, Class<T> clazz) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse config value for key: {}", configKey, e);
            return null;
        }
    }
}