package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业数据访问层
 */
@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {
    
} 