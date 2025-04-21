package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fq.yznu.eaps.entity.EmploymentInfo;
import com.fq.yznu.eaps.mapper.EmploymentInfoMapper;
import com.fq.yznu.eaps.service.EmploymentInfoService;
import com.fq.yznu.eaps.vo.EmploymentStatisticsVO;
import com.fq.yznu.eaps.vo.request.EmploymentInfoQueryReq;
import com.fq.yznu.eaps.util.ExcelExportUtil;
import com.fq.yznu.eaps.vo.export.EmploymentExportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 就业信息服务实现类
 */
@Service
public class EmploymentInfoServiceImpl extends ServiceImpl<EmploymentInfoMapper, EmploymentInfo>
        implements EmploymentInfoService {

    @Resource
    private EmploymentInfoMapper employmentInfoMapper;

    @Autowired
    private ExcelExportUtil excelExportUtil;

    @Override
    public IPage<EmploymentInfo> pageQuery(Page<EmploymentInfo> page, EmploymentInfoQueryReq query) {
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();

        // 设置查询条件
        wrapper.eq(EmploymentInfo::getDeleted, false);

        if (query.getEmploymentStatus() != null) {
            wrapper.eq(EmploymentInfo::getEmploymentStatus, query.getEmploymentStatus());
        }

        if (query.getVerifyStatus() != null) {
            wrapper.eq(EmploymentInfo::getVerifyStatus, query.getVerifyStatus());
        }

        if (query.getGraduationYear() != null) {
            wrapper.eq(EmploymentInfo::getGraduationYear, query.getGraduationYear());
        }

        if (query.getCollegeId() != null) {
            wrapper.eq(EmploymentInfo::getCollegeId, query.getCollegeId());
        }

        if (query.getMajorId() != null) {
            wrapper.eq(EmploymentInfo::getMajorId, query.getMajorId());
        }

        if (query.getClassId() != null) {
            wrapper.eq(EmploymentInfo::getClassId, query.getClassId());
        }

        if (query.getStudentId() != null) {
            wrapper.eq(EmploymentInfo::getStudentId, query.getStudentId());
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(EmploymentInfo::getStudentName, query.getKeyword())
                    .or()
                    .like(EmploymentInfo::getStudentId, query.getKeyword())
                    .or()
                    .like(EmploymentInfo::getCompanyName, query.getKeyword())
                    .or()
                    .like(EmploymentInfo::getPositionName, query.getKeyword()));
        }

        wrapper.orderByDesc(EmploymentInfo::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public EmploymentInfo getByStudentId(Long studentId, Integer graduationYear) {
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getStudentId, studentId)
                .eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getDeleted, false);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveEmploymentInfo(EmploymentInfo employmentInfo) {
        employmentInfo.setCreateTime(LocalDateTime.now());
        employmentInfo.setVerifyStatus(0); // 设置为待审核状态
        return save(employmentInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmploymentInfo(EmploymentInfo employmentInfo) {
        EmploymentInfo existInfo = getById(employmentInfo.getId());
        if (existInfo == null) {
            return false;
        }

        employmentInfo.setUpdateTime(LocalDateTime.now());
        employmentInfo.setVerifyStatus(0); // 更新后需要重新审核
        return updateById(employmentInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitEmploymentInfo(EmploymentInfo employmentInfo) {
        // 检查是否已存在记录
        EmploymentInfo existInfo = getByStudentId(employmentInfo.getStudentId(), employmentInfo.getGraduationYear());

        if (existInfo != null) {
            // 更新现有记录
            employmentInfo.setId(existInfo.getId());
            employmentInfo.setUpdateTime(LocalDateTime.now());
            employmentInfo.setVerifyStatus(0); // 重置为待审核状态
            return updateById(employmentInfo);
        } else {
            // 创建新记录
            employmentInfo.setCreateTime(LocalDateTime.now());
            employmentInfo.setVerifyStatus(0); // 设置为待审核状态
            return save(employmentInfo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyEmploymentInfo(Long id, Integer verifyStatus, String verifyRemark) {
        EmploymentInfo info = getById(id);
        if (info == null) {
            return false;
        }

        info.setVerifyStatus(verifyStatus);
        info.setVerifyRemark(verifyRemark);
        info.setVerifyTime(LocalDateTime.now());

        return updateById(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeEmploymentInfo(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemoveEmploymentInfo(List<Long> ids) {
        return removeBatchByIds(ids);
    }

    @Override
    public List<EmploymentStatisticsVO> getCollegeStatistics(Integer graduationYear) {
        List<Map<String, Object>> collegeStats = employmentInfoMapper.countEmploymentByCollege(graduationYear);
        return convertToStatisticsVO(collegeStats);
    }

    @Override
    public List<EmploymentStatisticsVO> getMajorStatistics(Long collegeId, Integer graduationYear) {
        List<Map<String, Object>> majorStats = employmentInfoMapper.countEmploymentByMajor(graduationYear, collegeId);
        return convertToStatisticsVO(majorStats);
    }

    @Override
    public List<EmploymentStatisticsVO> getClassStatistics(Long majorId, Integer graduationYear) {
        List<Map<String, Object>> classStats = employmentInfoMapper.countEmploymentByClass(graduationYear, majorId);
        return convertToStatisticsVO(classStats);
    }

    @Override
    public Map<String, Object> getEmploymentStatusDistribution(Integer graduationYear) {
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只统计已审核通过的
                .eq(EmploymentInfo::getDeleted, false);

        List<EmploymentInfo> employmentInfos = list(wrapper);
        Map<String, Object> result = new HashMap<>();

        // 计算各就业状态的人数
        Map<Integer, Long> statusCount = employmentInfos.stream()
                .collect(Collectors.groupingBy(
                        EmploymentInfo::getEmploymentStatus,
                        Collectors.counting()));

        // 转换为前端需要的格式
        List<Map<String, Object>> distribution = new ArrayList<>();

        // 状态：0未就业，1已就业，2升学，3创业，4灵活就业
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "未就业");
        statusMap.put(1, "已就业");
        statusMap.put(2, "升学");
        statusMap.put(3, "创业");
        statusMap.put(4, "灵活就业");

        for (Map.Entry<Integer, String> entry : statusMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getValue());
            item.put("value", statusCount.getOrDefault(entry.getKey(), 0L));
            distribution.add(item);
        }

        result.put("distribution", distribution);
        return result;
    }

    @Override
    public Map<String, Object> getCompanyTypeDistribution(Integer graduationYear) {
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只统计已审核通过的
                .eq(EmploymentInfo::getEmploymentStatus, 1) // 只统计已就业的
                .eq(EmploymentInfo::getDeleted, false);

        List<EmploymentInfo> employmentInfos = list(wrapper);
        Map<String, Object> result = new HashMap<>();

        // 计算各单位性质的人数
        Map<Integer, Long> typeCount = employmentInfos.stream()
                .filter(info -> info.getCompanyType() != null)
                .collect(Collectors.groupingBy(
                        EmploymentInfo::getCompanyType,
                        Collectors.counting()));

        // 转换为前端需要的格式
        List<Map<String, Object>> distribution = new ArrayList<>();

        // 单位性质：1国有企业，2民营企业，3外资企业，4政府机构，5事业单位，6其他
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(1, "国有企业");
        typeMap.put(2, "民营企业");
        typeMap.put(3, "外资企业");
        typeMap.put(4, "政府机构");
        typeMap.put(5, "事业单位");
        typeMap.put(6, "其他");

        for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getValue());
            item.put("value", typeCount.getOrDefault(entry.getKey(), 0L));
            distribution.add(item);
        }

        result.put("distribution", distribution);
        return result;
    }

    @Override
    public Map<String, Object> getRegionDistribution(Integer graduationYear) {
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只统计已审核通过的
                .eq(EmploymentInfo::getEmploymentStatus, 1) // 只统计已就业的
                .eq(EmploymentInfo::getDeleted, false);

        List<EmploymentInfo> employmentInfos = list(wrapper);
        Map<String, Object> result = new HashMap<>();

        // 计算各省份就业人数
        Map<String, Long> provinceCount = employmentInfos.stream()
                .filter(info -> StringUtils.isNotBlank(info.getWorkProvince()))
                .collect(Collectors.groupingBy(
                        EmploymentInfo::getWorkProvince,
                        Collectors.counting()));

        // 转换为前端需要的格式
        List<Map<String, Object>> distribution = new ArrayList<>();

        for (Map.Entry<String, Long> entry : provinceCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            distribution.add(item);
        }

        result.put("distribution", distribution);
        return result;
    }

    @Override
    public Map<String, Object> getSalaryDistribution(Integer graduationYear) {
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只统计已审核通过的
                .eq(EmploymentInfo::getEmploymentStatus, 1) // 只统计已就业的
                .eq(EmploymentInfo::getDeleted, false);

        List<EmploymentInfo> employmentInfos = list(wrapper);
        Map<String, Object> result = new HashMap<>();

        // 定义薪资区间
        String[] salaryRanges = {
                "3000以下",
                "3000-5000",
                "5000-8000",
                "8000-10000",
                "10000-15000",
                "15000-20000",
                "20000以上"
        };

        // 统计各薪资区间人数
        Map<String, Long> rangeCount = new HashMap<>();
        for (String range : salaryRanges) {
            rangeCount.put(range, 0L);
        }

        for (EmploymentInfo info : employmentInfos) {
            BigDecimal salary = info.getMonthlySalary();
            if (salary == null)
                continue;

            String range;
            int salaryInt = salary.intValue();
            if (salaryInt < 3000) {
                range = "3000以下";
            } else if (salaryInt < 5000) {
                range = "3000-5000";
            } else if (salaryInt < 8000) {
                range = "5000-8000";
            } else if (salaryInt < 10000) {
                range = "8000-10000";
            } else if (salaryInt < 15000) {
                range = "10000-15000";
            } else if (salaryInt < 20000) {
                range = "15000-20000";
            } else {
                range = "20000以上";
            }

            rangeCount.put(range, rangeCount.get(range) + 1);
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> distribution = new ArrayList<>();

        for (String range : salaryRanges) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", range);
            item.put("value", rangeCount.get(range));
            distribution.add(item);
        }

        result.put("distribution", distribution);
        return result;
    }

    /**
     * 将统计结果转换为VO对象
     */
    private List<EmploymentStatisticsVO> convertToStatisticsVO(List<Map<String, Object>> statisticsData) {
        if (statisticsData == null || statisticsData.isEmpty()) {
            return new ArrayList<>();
        }

        return statisticsData.stream().map(data -> {
            EmploymentStatisticsVO vo = new EmploymentStatisticsVO();

            // 设置ID和名称
            if (data.containsKey("id")) {
                vo.setId(Long.valueOf(data.get("id").toString()));
            }
            if (data.containsKey("name")) {
                vo.setName(data.get("name").toString());
            }

            // 设置总人数
            if (data.containsKey("total_count")) {
                vo.setTotalCount(Integer.valueOf(data.get("total_count").toString()));
            }

            // 设置已就业人数
            if (data.containsKey("employed_count")) {
                vo.setEmployedCount(Integer.valueOf(data.get("employed_count").toString()));
            }

            // 设置就业率
            if (vo.getTotalCount() != null && vo.getTotalCount() > 0 && vo.getEmployedCount() != null) {
                vo.setEmploymentRate(vo.getEmployedCount() * 100.0 / vo.getTotalCount());
            } else {
                vo.setEmploymentRate(0.0);
            }

            // 设置未就业人数
            if (data.containsKey("unemployed_count")) {
                vo.setUnemployedCount(Integer.valueOf(data.get("unemployed_count").toString()));
            } else if (vo.getTotalCount() != null && vo.getEmployedCount() != null) {
                vo.setUnemployedCount(vo.getTotalCount() - vo.getEmployedCount());
            }

            // 设置考研人数
            if (data.containsKey("postgraduate_count")) {
                vo.setPostgraduateCount(Integer.valueOf(data.get("postgraduate_count").toString()));
            }

            // 设置考研率
            if (vo.getTotalCount() != null && vo.getTotalCount() > 0 && vo.getPostgraduateCount() != null) {
                vo.setPostgraduateRate(vo.getPostgraduateCount() * 100.0 / vo.getTotalCount());
            } else {
                vo.setPostgraduateRate(0.0);
            }

            // 设置创业人数
            if (data.containsKey("entrepreneurship_count")) {
                vo.setEntrepreneurshipCount(Integer.valueOf(data.get("entrepreneurship_count").toString()));
            }

            // 设置创业率
            if (vo.getTotalCount() != null && vo.getTotalCount() > 0 && vo.getEntrepreneurshipCount() != null) {
                vo.setEntrepreneurshipRate(vo.getEntrepreneurshipCount() * 100.0 / vo.getTotalCount());
            } else {
                vo.setEntrepreneurshipRate(0.0);
            }

            // 设置其他去向人数
            if (data.containsKey("other_count")) {
                vo.setOtherCount(Integer.valueOf(data.get("other_count").toString()));
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String generateEmploymentReport(Integer graduationYear) {
        // 查询就业信息
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只导出已审核通过的
                .eq(EmploymentInfo::getDeleted, false)
                .orderByDesc(EmploymentInfo::getCreateTime);

        List<EmploymentInfo> employmentInfoList = list(wrapper);

        // 转换为导出VO
        List<EmploymentExportVO> exportVOList = employmentInfoList.stream()
                .map(this::convertToExportVO)
                .collect(Collectors.toList());

        // 导出Excel
        String fileName = "就业信息统计报表_" + graduationYear + "届";
        return excelExportUtil.exportExcel(exportVOList, EmploymentExportVO.class, fileName);
    }

    /**
     * 将就业信息转换为导出VO
     */
    private EmploymentExportVO convertToExportVO(EmploymentInfo info) {
        EmploymentExportVO vo = new EmploymentExportVO();

        // 基本信息
        vo.setStudentId(info.getStudentId().toString());
        vo.setStudentName(info.getStudentName());
        vo.setGraduationYear(info.getGraduationYear());

        // 学生信息需要查询学生表(这里简化处理，实际应该关联学生表获取)
        vo.setCollege(info.getCollegeName());
        vo.setMajor(info.getMajorName());
        vo.setClassName(info.getClassName());

        // 就业状态和类型转换
        vo.setEmploymentStatus(convertEmploymentStatus(info.getEmploymentStatus()));
        vo.setEmploymentType(convertEmploymentType(info.getEmploymentType()));

        // 公司信息
        vo.setCompanyName(info.getCompanyName());
        vo.setCompanyType(convertCompanyType(info.getCompanyType()));
        vo.setWorkCity(info.getWorkCity());
        vo.setWorkProvince(info.getWorkProvince());
        vo.setPositionName(info.getPositionName());
        vo.setMonthlySalary(info.getMonthlySalary());
        vo.setContractDuration(info.getContractDuration());

        // 升学信息
        vo.setFurtherSchool(info.getFurtherSchool());
        vo.setFurtherMajor(info.getFurtherMajor());
        vo.setEducationLevel(convertEducationLevel(info.getEducationLevel()));

        // 创业信息
        vo.setEntrepreneurshipProject(info.getEntrepreneurshipProject());

        // 联系信息
        vo.setContactPhone(info.getContactPhone());
        vo.setContactEmail(info.getContactEmail());

        // 审核状态
        vo.setVerifyStatus(convertVerifyStatus(info.getVerifyStatus()));

        return vo;
    }

    /**
     * 转换就业状态
     */
    private String convertEmploymentStatus(Integer status) {
        if (status == null)
            return "";
        switch (status) {
            case 0:
                return "未就业";
            case 1:
                return "已就业";
            case 2:
                return "升学";
            case 3:
                return "创业";
            case 4:
                return "灵活就业";
            default:
                return "未知";
        }
    }

    /**
     * 转换就业类型
     */
    private String convertEmploymentType(Integer type) {
        if (type == null)
            return "";
        switch (type) {
            case 1:
                return "合同就业";
            case 2:
                return "灵活就业";
            default:
                return "未知";
        }
    }

    /**
     * 转换单位性质
     */
    private String convertCompanyType(Integer type) {
        if (type == null)
            return "";
        switch (type) {
            case 1:
                return "国有企业";
            case 2:
                return "民营企业";
            case 3:
                return "外资企业";
            case 4:
                return "政府机构";
            case 5:
                return "事业单位";
            case 6:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 转换学历层次
     */
    private String convertEducationLevel(Integer level) {
        if (level == null)
            return "";
        switch (level) {
            case 1:
                return "硕士";
            case 2:
                return "博士";
            case 3:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 转换审核状态
     */
    private String convertVerifyStatus(Integer status) {
        if (status == null)
            return "";
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知";
        }
    }

    @Override
    public String generateCollegeReport(Long collegeId, Integer graduationYear) {
        // 查询学院下的就业信息
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getCollegeId, collegeId)
                .eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只导出已审核通过的
                .eq(EmploymentInfo::getDeleted, false)
                .orderByDesc(EmploymentInfo::getCreateTime);

        List<EmploymentInfo> employmentInfoList = list(wrapper);

        // 转换为导出VO
        List<EmploymentExportVO> exportVOList = employmentInfoList.stream()
                .map(this::convertToExportVO)
                .collect(Collectors.toList());

        // 导出Excel
        String fileName = "就业信息统计报表_" + graduationYear + "届_按学院";
        return excelExportUtil.exportExcel(exportVOList, EmploymentExportVO.class, fileName);
    }

    @Override
    public String generateMajorReport(Long majorId, Integer graduationYear) {
        // 查询专业下的就业信息
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getMajorId, majorId)
                .eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只导出已审核通过的
                .eq(EmploymentInfo::getDeleted, false)
                .orderByDesc(EmploymentInfo::getCreateTime);

        List<EmploymentInfo> employmentInfoList = list(wrapper);

        // 转换为导出VO
        List<EmploymentExportVO> exportVOList = employmentInfoList.stream()
                .map(this::convertToExportVO)
                .collect(Collectors.toList());

        // 导出Excel
        String fileName = "就业信息统计报表_" + graduationYear + "届_按专业";
        return excelExportUtil.exportExcel(exportVOList, EmploymentExportVO.class, fileName);
    }

    @Override
    public String generateClassReport(Long classId, Integer graduationYear) {
        // 查询班级下的就业信息
        LambdaQueryWrapper<EmploymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmploymentInfo::getClassId, classId)
                .eq(EmploymentInfo::getGraduationYear, graduationYear)
                .eq(EmploymentInfo::getVerifyStatus, 1) // 只导出已审核通过的
                .eq(EmploymentInfo::getDeleted, false)
                .orderByDesc(EmploymentInfo::getCreateTime);

        List<EmploymentInfo> employmentInfoList = list(wrapper);

        // 转换为导出VO
        List<EmploymentExportVO> exportVOList = employmentInfoList.stream()
                .map(this::convertToExportVO)
                .collect(Collectors.toList());

        // 导出Excel
        String fileName = "就业信息统计报表_" + graduationYear + "届_按班级";
        return excelExportUtil.exportExcel(exportVOList, EmploymentExportVO.class, fileName);
    }
}