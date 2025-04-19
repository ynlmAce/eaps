package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.JobPosition;
import java.util.Map;

/**
 * 职位服务接口
 */
public interface JobPositionService {

    /**
     * 发布职位
     *
     * @param jobPosition 职位信息
     */
    void publishPosition(JobPosition jobPosition);

    /**
     * 更新职位信息
     *
     * @param jobPosition 职位信息
     */
    void updatePosition(JobPosition jobPosition);

    /**
     * 删除职位
     *
     * @param id 职位ID
     */
    void deletePosition(Long id);

    /**
     * 审核职位
     *
     * @param id     职位ID
     * @param status 审核状态（1：通过，2：拒绝）
     * @param reason 拒绝原因
     */
    void auditPosition(Long id, Integer status, String reason);

    /**
     * 获取职位列表
     *
     * @param queryParams 查询参数
     * @return 职位列表与分页信息
     */
    Map<String, Object> getPositionList(Map<String, Object> queryParams);

    /**
     * 获取职位详情
     *
     * @param id 职位ID
     * @return 职位详情
     */
    JobPosition getPositionDetail(Long id);
} 