package com.fq.yznu.eaps.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 存储文件
     *
     * @param file 文件
     * @param directory 存储目录
     * @return 文件访问URL
     * @throws IOException 存储异常
     */
    String storeFile(MultipartFile file, String directory) throws IOException;
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 获取文件访问URL前缀
     *
     * @return URL前缀
     */
    String getFileAccessPrefix();
} 