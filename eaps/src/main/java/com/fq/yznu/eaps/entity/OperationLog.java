package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_log")
public class OperationLog extends BaseEntity {
    
    /**
     * 模块标题
     */
    private String title;
    
    /**
     * 业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8清空数据）
     */
    private Integer businessType;
    
    /**
     * 方法名称
     */
    private String method;
    
    /**
     * 请求方式
     */
    private String requestMethod;
    
    /**
     * 操作类型（0其它 1后台用户 2手机端用户）
     */
    private Integer operatorType;
    
    /**
     * 操作人员
     */
    private String operatorName;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 请求URL
     */
    private String operationUrl;
    
    /**
     * 主机地址
     */
    private String operationIp;
    
    /**
     * 操作地点
     */
    private String operationLocation;
    
    /**
     * 请求参数
     */
    private String operationParam;
    
    /**
     * 返回参数
     */
    private String jsonResult;
    
    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;
    
    /**
     * 错误消息
     */
    private String errorMsg;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
    /**
     * 消耗时间
     */
    private Long costTime;
} 