package com.fq.yznu.eaps.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fq.yznu.eaps.entity.EmploymentInfo;
import com.fq.yznu.eaps.vo.EmploymentStatisticsVO;
import com.fq.yznu.eaps.vo.request.EmploymentInfoQueryReq;

import java.util.List;
import java.util.Map;

/**
 * 就业信息服务接口
 */
public interface EmploymentInfoService extends IService<EmploymentInfo> {

    /**
     * 分页查询就业信息
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<EmploymentInfo> pageQuery(Page<EmploymentInfo> page, EmploymentInfoQueryReq query);

    /**
     * 保存就业信息
     *
     * @param employmentInfo 就业信息
     * @return 是否成功
     */
    boolean saveEmploymentInfo(EmploymentInfo employmentInfo);

    /**
     * 更新就业信息
     *
     * @param employmentInfo 就业信息
     * @return 是否成功
     */
    boolean updateEmploymentInfo(EmploymentInfo employmentInfo);

    /**
     * 审核就业信息
     *
     * @param id           就业信息ID
     * @param verifyStatus 审核状态
     * @param verifyRemark 审核备注
     * @return 是否成功
     */
    boolean verifyEmploymentInfo(Long id, Integer verifyStatus, String verifyRemark);

    /**
     * 删除就业信息
     *
     * @param id 就业信息ID
     * @return 是否成功
     */
    boolean removeEmploymentInfo(Long id);

    /**
     * 批量删除就业信息
     *
     * @param ids 就业信息ID列表
     * @return 是否成功
     */
    boolean batchRemoveEmploymentInfo(List<Long> ids);

    /**
     * 获取学院就业统计
     *
     * @param graduationYear 毕业年份
     * @return 各学院就业统计
     */
    List<EmploymentStatisticsVO> getCollegeStatistics(Integer graduationYear);

    /**
     * 获取专业就业统计
     *
     * @param collegeId      学院ID
     * @param graduationYear 毕业年份
     * @return 专业就业统计
     */
    List<EmploymentStatisticsVO> getMajorStatistics(Long collegeId, Integer graduationYear);

    /**
     * 获取班级就业统计
     *
     * @param majorId        专业ID
     * @param graduationYear 毕业年份
     * @return 班级就业统计
     */
    List<EmploymentStatisticsVO> getClassStatistics(Long majorId, Integer graduationYear);

    /**
     * 获取就业状态分布
     *
     * @param graduationYear 毕业年份
     * @return 就业状态分布
     */
    Map<String, Object> getEmploymentStatusDistribution(Integer graduationYear);

    /**
     * 获取就业单位性质分布
     *
     * @param graduationYear 毕业年份
     * @return 就业单位性质分布
     */
    Map<String, Object> getCompanyTypeDistribution(Integer graduationYear);

    /**
     * 获取就业区域分布
     *
     * @param graduationYear 毕业年份
     * @return 就业区域分布
     */
    Map<String, Object> getRegionDistribution(Integer graduationYear);

    /**
     * 获取薪资区间分布
     *
     * @param graduationYear 毕业年份
     * @return 薪资区间分布
     */
    Map<String, Object> getSalaryDistribution(Integer graduationYear);

    /**
     * 生成就业统计报表
     *
     * @param graduationYear 毕业年份
     * @return 报表文件路径
     */
    String generateEmploymentReport(Integer graduationYear);

    /**
     * 按学院生成就业报表
     *
     * @param collegeId      学院ID
     * @param graduationYear 毕业年份
     * @return 报表文件路径
     */
    String generateCollegeReport(Long collegeId, Integer graduationYear);

    /**
     * 按专业生成就业报表
     *
     * @param majorId        专业ID
     * @param graduationYear 毕业年份
     * @return 报表文件路径
     */
    String generateMajorReport(Long majorId, Integer graduationYear);

    /**
     * 按班级生成就业报表
     *
     * @param classId        班级ID
     * @param graduationYear 毕业年份
     * @return 报表文件路径
     */
    String generateClassReport(Long classId, Integer graduationYear);

    /**
     * 获取学生的就业信息
     *
     * @param studentId      学生ID
     * @param graduationYear 毕业年份
     * @return 就业信息
     */
    EmploymentInfo getByStudentId(Long studentId, Integer graduationYear);

    /**
     * 提交就业信息（学生用）
     *
     * @param employmentInfo 就业信息
     * @return 是否成功
     */
    boolean submitEmploymentInfo(EmploymentInfo employmentInfo);
}