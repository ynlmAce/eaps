package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.service.FileStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@Api(tags = "文件上传管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @param directory 目录
     * @return 文件URL
     */
    @ApiOperation("上传单个文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Map<String, String>> uploadFile(
            @ApiParam(value = "文件", required = true) 
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "存储目录", required = false)
            @RequestParam(value = "directory", required = false) String directory) {
        
        log.info("上传文件: {}, 目录: {}", file.getOriginalFilename(), directory);
        
        try {
            String fileUrl = fileStorageService.storeFile(file, directory);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("originalFilename", file.getOriginalFilename());
            
            return ResponseResult.success(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseResult.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传多个文件
     *
     * @param files 文件列表
     * @param directory 目录
     * @return 文件URL列表
     */
    @ApiOperation("上传多个文件")
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<List<Map<String, String>>> uploadFiles(
            @ApiParam(value = "文件列表", required = true) 
            @RequestParam("files") MultipartFile[] files,
            @ApiParam(value = "存储目录", required = false)
            @RequestParam(value = "directory", required = false) String directory) {
        
        log.info("批量上传文件: {} 个, 目录: {}", files.length, directory);
        
        List<Map<String, String>> resultList = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String fileUrl = fileStorageService.storeFile(file, directory);
                
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("url", fileUrl);
                fileInfo.put("originalFilename", file.getOriginalFilename());
                
                resultList.add(fileInfo);
            } catch (IOException e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                // 继续处理其他文件
            }
        }
        
        return ResponseResult.success(resultList);
    }
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @ApiOperation("删除文件")
    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseResult<Void> deleteFile(
            @ApiParam(value = "文件URL", required = true) 
            @RequestParam("fileUrl") @NotNull(message = "文件URL不能为空") String fileUrl) {
        
        log.info("删除文件: {}", fileUrl);
        
        boolean success = fileStorageService.deleteFile(fileUrl);
        if (!success) {
            return ResponseResult.error("文件删除失败");
        }
        
        return ResponseResult.success();
    }
} 