package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fq.yznu.eaps.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生数据访问层
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
} 