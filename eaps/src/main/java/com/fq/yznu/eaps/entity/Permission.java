package com.fq.yznu.eaps.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 权限类型（0菜单 1按钮 2API）
     */
    private Integer permissionType;
    
    /**
     * 权限标识
     */
    private String permissionKey;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 路由参数
     */
    private String query;
    
    /**
     * 是否为外链（0否 1是）
     */
    private Integer isFrame;
    
    /**
     * 是否缓存（0否 1是）
     */
    private Integer isCache;
    
    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;
    
    /**
     * 菜单状态（0显示 1隐藏）
     */
    private Integer visible;
    
    /**
     * 菜单状态（0正常 1停用）
     */
    private Integer status;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
} 