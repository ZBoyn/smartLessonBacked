package com.neu.smartLesson.service;

import com.neu.smartLesson.exception.RegistrationException;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootUploadPath; // e.g., D:/smartcourse-uploads

    // 从 .env (via application.properties) 注入上传目录
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootUploadPath = Paths.get(uploadDir);

        // 启动时检查目录是否存在，如果不存在则创建
        try {
            Files.createDirectories(this.rootUploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法初始化文件上传目录!", e);
        }
    }

    /**
     * 存储文件
     * @param file 用户上传的文件
     * @return 存储在服务器上的相对路径 (e.g., /2025/11/abc-123.mp4)
     */
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RegistrationException("上传失败：文件为空。");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // 安全起见，我们重命名文件，防止路径遍历攻击
            // e.g., "video.mp4" -> "uuid1234-video.mp4"
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String safeFilename = UUID.randomUUID().toString() + "." + fileExtension;

            // 我们将文件存储在根目录 (V2.0可以按日期分目录)
            Path targetLocation = this.rootUploadPath.resolve(safeFilename);

            // 将文件流复制到目标位置
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return safeFilename;

        } catch (IOException ex) {
            throw new RegistrationException("存储文件失败: " + ex.getMessage());
        }
    }

    /**
     * 加载文件作为资源
     * @param filename 存储在服务器上的文件名
     * @return Spring Resource 对象
     */
    public org.springframework.core.io.Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.rootUploadPath.resolve(filename).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("文件未找到: " + filename);
            }
        } catch (Exception ex) {
            throw new ResourceNotFoundException("文件未找到: " + filename);
        }
    }

    /**
     * 获取文件类型 (e.g., video, pdf, ppt)
     */
    public String getResourceType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.startsWith("video")) {
            return "video";
        }
        if (contentType.equals("application/pdf")) {
            return "pdf";
        }
        if (contentType.contains("presentation") || contentType.contains("powerpoint")) {
            return "ppt";
        }
        if (contentType.startsWith("text")) {
            return "text";
        }
        return "other";
    }
}