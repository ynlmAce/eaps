package com.fq.yznu.eaps.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Excel导出工具类
 */
@Slf4j
@Component
public class ExcelExportUtil {

    @Value("${file.export.path:./export}")
    private String exportPath;

    /**
     * 导出Excel文件
     *
     * @param data 数据列表
     * @param clazz 数据类型
     * @param fileName 文件名前缀
     * @param <T> 数据类型
     * @return 导出文件路径
     */
    public <T> String exportExcel(List<T> data, Class<T> clazz, String fileName) {
        try {
            // 确保导出目录存在
            Path directoryPath = Paths.get(exportPath);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 生成唯一文件名
            String uniqueFileName = generateUniqueFileName(fileName);
            String filePath = exportPath + File.separator + uniqueFileName;

            // 导出Excel
            ExcelWriterBuilder writerBuilder = EasyExcel.write(filePath, clazz)
                    .excelType(ExcelTypeEnum.XLSX)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());

            writerBuilder.sheet("数据").doWrite(data);

            log.info("Excel导出成功，文件路径：{}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 生成唯一文件名
     *
     * @param prefix 文件名前缀
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "_" + timestamp + "_" + uuid + ".xlsx";
    }
} 