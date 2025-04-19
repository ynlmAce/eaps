package com.fq.yznu.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.entity.EnterprisePromotion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 企业宣传信息Mapper接口
 */
@Mapper
public interface EnterprisePromotionMapper extends BaseMapper<EnterprisePromotion> {

    /**
     * 分页查询企业宣传信息
     *
     * @param page 分页参数
     * @param enterpriseId 企业ID
     * @param type 宣传类型
     * @param status 状态
     * @param keyword 关键词
     * @return 分页数据
     */
    IPage<EnterprisePromotion> selectPromotionPage(Page<EnterprisePromotion> page,
                                                  @Param("enterpriseId") Long enterpriseId,
                                                  @Param("type") Integer type,
                                                  @Param("status") Integer status,
                                                  @Param("keyword") String keyword);

    /**
     * 增加浏览量
     *
     * @param id 宣传信息ID
     * @return 是否成功
     */
    @Update("UPDATE enterprise_promotion SET view_count = view_count + 1 WHERE id = #{id}")
    boolean incrementViewCount(@Param("id") Long id);
    
    /**
     * 查询待审核的宣传信息数量
     *
     * @return 待审核数量
     */
    Integer countPendingReview();
} 