package com.fq.yznu.eaps.service.impl;

import com.fq.yznu.eaps.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储服务实现类
 */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:/uploads}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:http://localhost:8080/uploads}")
    private String fileUrlPrefix;

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("无法存储空文件");
        }

        // 获取文件名
        String originalFilename = file.getOriginalFilename();
        
        // 判断文件名是否为空
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("文件名不能为空");
        }
        
        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        // 检查文件类型
        if (!isValidFileExtension(extension)) {
            throw new IOException("不支持的文件类型: " + extension);
        }

        // 生成唯一文件名 - 使用UUID并保留原始扩展名
        String newFilename = UUID.randomUUID().toString() + extension;
        
        // 按日期创建目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = StringUtils.hasText(directory) 
                ? directory + "/" + datePath 
                : datePath;
        
        // 创建目标目录
        Path targetDir = Paths.get(uploadDir, relativePath);
        Files.createDirectories(targetDir);
        
        // 目标文件路径
        Path targetPath = targetDir.resolve(newFilename);
        
        // 保存文件
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("文件已保存: {}", targetPath);
        
        // 返回可访问的URL
        return fileUrlPrefix + "/" + relativePath + "/" + newFilename;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl) || !fileUrl.startsWith(fileUrlPrefix)) {
            log.warn("无效的文件URL: {}", fileUrl);
            return false;
        }
        
        try {
            // 从URL中提取相对路径
            String relativePath = fileUrl.substring(fileUrlPrefix.length());
            
            // 构建文件路径
            Path filePath = Paths.get(uploadDir, relativePath);
            
            // 删除文件
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getFileAccessPrefix() {
        return fileUrlPrefix;
    }
    
    /**
     * 检查文件扩展名是否合法
     *
     * @param extension 文件扩展名
     * @return 是否合法
     */
    private boolean isValidFileExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }
        
        // 允许的文件类型列表
        String[] allowedExtensions = {
                ".jpg", ".jpeg", ".png", ".gif", ".bmp", // 图片
                ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", // 文档
                ".txt", ".csv", ".zip", ".rar" // 其他常见类型
        };
        
        String lowerExtension = extension.toLowerCase();
        for (String allowed : allowedExtensions) {
            if (lowerExtension.equals(allowed)) {
                return true;
            }
        }
        
        return false;
    }
} 