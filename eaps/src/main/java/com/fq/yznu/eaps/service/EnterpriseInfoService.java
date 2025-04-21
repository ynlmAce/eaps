package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.EnterpriseInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 企业信息服务接口
 */
public interface EnterpriseInfoService {

    /**
     * 获取企业基本信息
     *
     * @param id 企业ID
     * @return 企业信息
     */
    EnterpriseInfo getEnterpriseInfo(Long id);

    /**
     * 获取当前登录企业信息
     *
     * @return 企业信息
     */
    EnterpriseInfo getCurrentEnterpriseInfo();

    /**
     * 更新企业信息
     *
     * @param enterpriseInfo 企业信息
     * @param license        营业执照文件（可选）
     * @param logo           企业Logo文件（可选）
     */
    void updateEnterpriseInfo(EnterpriseInfo enterpriseInfo, MultipartFile license, MultipartFile logo);

    /**
     * 获取企业列表
     *
     * @param params 查询参数
     * @return 企业列表与分页信息
     */
    Map<String, Object> getEnterpriseList(Map<String, Object> params);

    /**
     * 获取企业详情
     *
     * @param id 企业ID
     * @return 企业详情
     */
    Map<String, Object> getEnterpriseDetail(Long id);

    /**
     * 审核企业信息
     *
     * @param id            企业ID
     * @param verifyStatus  审核状态（1：通过，2：拒绝）
     * @param verifyComment 审核意见
     */
    void verifyEnterprise(Long id, Integer verifyStatus, String verifyComment);

    /**
     * 获取待审核的企业列表
     *
     * @param params 查询参数
     * @return 企业列表与分页信息
     */
    Map<String, Object> getPendingEnterpriseList(Map<String, Object> params);
} 