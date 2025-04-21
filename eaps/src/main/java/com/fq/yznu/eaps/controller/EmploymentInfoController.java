package com.fq.yznu.eaps.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.EmploymentInfo;
import com.fq.yznu.eaps.service.EmploymentInfoService;
import com.fq.yznu.eaps.vo.EmploymentStatisticsVO;
import com.fq.yznu.eaps.vo.request.EmploymentInfoQueryReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 就业信息控制器
 */
@RestController
@RequestMapping("/employment")
public class EmploymentInfoController {

    @Autowired
    private EmploymentInfoService employmentInfoService;

    /**
     * 分页查询就业信息
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<IPage<EmploymentInfo>> pageQuery(EmploymentInfoQueryReq query) {
        Page<EmploymentInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<EmploymentInfo> result = employmentInfoService.pageQuery(page, query);
        return ResponseResult.success(result);
    }

    /**
     * 获取就业信息详情
     *
     * @param id 就业信息ID
     * @return 就业信息详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR', 'ROLE_STUDENT')")
    public ResponseResult<EmploymentInfo> getById(@PathVariable Long id) {
        EmploymentInfo info = employmentInfoService.getById(id);
        return ResponseResult.success(info);
    }

    /**
     * 获取学生的就业信息
     *
     * @param studentId      学生ID
     * @param graduationYear 毕业年份
     * @return 就业信息
     */
    @GetMapping("/student")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR', 'ROLE_STUDENT')")
    public ResponseResult<EmploymentInfo> getByStudentId(
            @RequestParam Long studentId,
            @RequestParam Integer graduationYear) {
        EmploymentInfo info = employmentInfoService.getByStudentId(studentId, graduationYear);
        return ResponseResult.success(info);
    }

    /**
     * 提交就业信息（学生）
     *
     * @param employmentInfo 就业信息
     * @return 成功响应
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseResult<Void> submitEmploymentInfo(@RequestBody @Valid EmploymentInfo employmentInfo) {
        boolean success = employmentInfoService.submitEmploymentInfo(employmentInfo);
        return success ? ResponseResult.success() : ResponseResult.error("提交就业信息失败");
    }

    /**
     * 新增就业信息（管理员）
     *
     * @param employmentInfo 就业信息
     * @return 成功响应
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> saveEmploymentInfo(@RequestBody @Valid EmploymentInfo employmentInfo) {
        boolean success = employmentInfoService.saveEmploymentInfo(employmentInfo);
        return success ? ResponseResult.success() : ResponseResult.error("新增就业信息失败");
    }

    /**
     * 更新就业信息（管理员）
     *
     * @param id             就业信息ID
     * @param employmentInfo 就业信息
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> updateEmploymentInfo(
            @PathVariable Long id,
            @RequestBody @Valid EmploymentInfo employmentInfo) {
        employmentInfo.setId(id);
        boolean success = employmentInfoService.updateEmploymentInfo(employmentInfo);
        return success ? ResponseResult.success() : ResponseResult.error("更新就业信息失败");
    }

    /**
     * 审核就业信息
     *
     * @param id           就业信息ID
     * @param verifyStatus 审核状态
     * @param verifyRemark 审核备注
     * @return 成功响应
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<Void> verifyEmploymentInfo(
            @PathVariable Long id,
            @RequestParam Integer verifyStatus,
            @RequestParam String verifyRemark) {
        boolean success = employmentInfoService.verifyEmploymentInfo(id, verifyStatus, verifyRemark);
        return success ? ResponseResult.success() : ResponseResult.error("审核就业信息失败");
    }

    /**
     * 删除就业信息
     *
     * @param id 就业信息ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> removeEmploymentInfo(@PathVariable Long id) {
        boolean success = employmentInfoService.removeEmploymentInfo(id);
        return success ? ResponseResult.success() : ResponseResult.error("删除就业信息失败");
    }

    /**
     * 批量删除就业信息
     *
     * @param ids 就业信息ID列表
     * @return 成功响应
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> batchRemoveEmploymentInfo(@RequestBody List<Long> ids) {
        boolean success = employmentInfoService.batchRemoveEmploymentInfo(ids);
        return success ? ResponseResult.success() : ResponseResult.error("批量删除就业信息失败");
    }

    /**
     * 获取学院就业统计
     *
     * @param graduationYear 毕业年份
     * @return 各学院就业统计
     */
    @GetMapping("/statistics/college")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<List<EmploymentStatisticsVO>> getCollegeStatistics(
            @RequestParam Integer graduationYear) {
        List<EmploymentStatisticsVO> result = employmentInfoService.getCollegeStatistics(graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 获取专业就业统计
     *
     * @param collegeId      学院ID
     * @param graduationYear 毕业年份
     * @return 专业就业统计
     */
    @GetMapping("/statistics/major")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<List<EmploymentStatisticsVO>> getMajorStatistics(
            @RequestParam Long collegeId,
            @RequestParam Integer graduationYear) {
        List<EmploymentStatisticsVO> result = employmentInfoService.getMajorStatistics(collegeId, graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 获取班级就业统计
     *
     * @param majorId        专业ID
     * @param graduationYear 毕业年份
     * @return 班级就业统计
     */
    @GetMapping("/statistics/class")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<List<EmploymentStatisticsVO>> getClassStatistics(
            @RequestParam Long majorId,
            @RequestParam Integer graduationYear) {
        List<EmploymentStatisticsVO> result = employmentInfoService.getClassStatistics(majorId, graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 获取就业状态分布
     *
     * @param graduationYear 毕业年份
     * @return 就业状态分布
     */
    @GetMapping("/statistics/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<Map<String, Object>> getEmploymentStatusDistribution(
            @RequestParam Integer graduationYear) {
        Map<String, Object> result = employmentInfoService.getEmploymentStatusDistribution(graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 获取就业单位性质分布
     *
     * @param graduationYear 毕业年份
     * @return 就业单位性质分布
     */
    @GetMapping("/statistics/company-type")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<Map<String, Object>> getCompanyTypeDistribution(
            @RequestParam Integer graduationYear) {
        Map<String, Object> result = employmentInfoService.getCompanyTypeDistribution(graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 获取就业区域分布
     *
     * @param graduationYear 毕业年份
     * @return 就业区域分布
     */
    @GetMapping("/statistics/region")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<Map<String, Object>> getRegionDistribution(
            @RequestParam Integer graduationYear) {
        Map<String, Object> result = employmentInfoService.getRegionDistribution(graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 获取薪资区间分布
     *
     * @param graduationYear 毕业年份
     * @return 薪资区间分布
     */
    @GetMapping("/statistics/salary")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<Map<String, Object>> getSalaryDistribution(
            @RequestParam Integer graduationYear) {
        Map<String, Object> result = employmentInfoService.getSalaryDistribution(graduationYear);
        return ResponseResult.success(result);
    }

    /**
     * 生成就业报表
     *
     * @param graduationYear 毕业年份
     * @return 报表文件路径
     */
    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<String> generateEmploymentReport(
            @RequestParam Integer graduationYear) {
        String filePath = employmentInfoService.generateEmploymentReport(graduationYear);
        return ResponseResult.success(filePath);
    }

    /**
     * 下载就业报表
     *
     * @param graduationYear 毕业年份
     * @param response       HTTP响应
     */
    @GetMapping("/report/download")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public void downloadEmploymentReport(
            @RequestParam Integer graduationYear,
            HttpServletResponse response) throws IOException {
        String filePath = employmentInfoService.generateEmploymentReport(graduationYear);
        File file = new File(filePath);

        if (!file.exists()) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 500, \"message\": \"报表文件不存在\"}");
            return;
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("就业信息统计报表_" + graduationYear + "届.xlsx", StandardCharsets.UTF_8.toString());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        // 写入响应流
        try (FileInputStream inputStream = new FileInputStream(file);
                OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 500, \"message\": \"报表下载失败: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 按学院生成就业报表
     *
     * @param collegeId      学院ID
     * @param graduationYear 毕业年份
     * @param response       HTTP响应
     */
    @GetMapping("/report/college")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<String> generateCollegeReport(
            @RequestParam Long collegeId,
            @RequestParam Integer graduationYear) {
        String filePath = employmentInfoService.generateCollegeReport(collegeId, graduationYear);
        return ResponseResult.success(filePath);
    }

    /**
     * 按专业生成就业报表
     *
     * @param majorId        专业ID
     * @param graduationYear 毕业年份
     * @param response       HTTP响应
     */
    @GetMapping("/report/major")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<String> generateMajorReport(
            @RequestParam Long majorId,
            @RequestParam Integer graduationYear) {
        String filePath = employmentInfoService.generateMajorReport(majorId, graduationYear);
        return ResponseResult.success(filePath);
    }

    /**
     * 按班级生成就业报表
     *
     * @param classId        班级ID
     * @param graduationYear 毕业年份
     * @param response       HTTP响应
     */
    @GetMapping("/report/class")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_COUNSELOR')")
    public ResponseResult<String> generateClassReport(
            @RequestParam Long classId,
            @RequestParam Integer graduationYear) {
        String filePath = employmentInfoService.generateClassReport(classId, graduationYear);
        return ResponseResult.success(filePath);
    }
}