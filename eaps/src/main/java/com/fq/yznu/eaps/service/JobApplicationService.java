package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.JobApplication;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 职位申请服务接口
 */
public interface JobApplicationService {

    /**
     * 提交职位申请
     *
     * @param application 申请信息
     * @param resume      简历文件
     */
    void submitApplication(JobApplication application, MultipartFile resume);

    /**
     * 获取我的申请列表
     *
     * @param params 查询参数
     * @return 申请列表与分页信息
     */
    Map<String, Object> getMyApplicationList(Map<String, Object> params);

    /**
     * 获取申请详情
     *
     * @param id 申请ID
     * @return 申请详情
     */
    Map<String, Object> getApplicationDetail(Long id);

    /**
     * 撤销申请
     *
     * @param id 申请ID
     */
    void cancelApplication(Long id);

    /**
     * 获取企业的申请列表
     *
     * @param params 查询参数
     * @return 申请列表与分页信息
     */
    Map<String, Object> getEnterpriseApplicationList(Map<String, Object> params);

    /**
     * 更新申请状态
     *
     * @param id          申请ID
     * @param status      状态
     * @param feedback    反馈信息
     * @param extraParams 额外参数，如面试信息等
     */
    void updateApplicationStatus(Long id, Integer status, String feedback, Map<String, Object> extraParams);
} 