package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 图片上传
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        if (!FileUtil.isImage(file)) {
            return Result.error("仅支持上传图片文件");
        }
        try {
            String url = FileUtil.saveFile(file, uploadDir);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件上传失败");
        }
    }

    /**
     * 通用文件上传
     */
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        try {
            String url = FileUtil.saveFile(file, uploadDir);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件上传失败");
        }
    }
}
