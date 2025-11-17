package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.StreamResourceDto; // 导入
import com.neu.smartLesson.model.User; // 导入
import com.neu.smartLesson.service.ResourceService; // 【修改】
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileStreamController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 安全地流式传输文件
     */
    @GetMapping("/resources/{resourceId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<Resource> getResource(
            @PathVariable Integer resourceId, @AuthenticationPrincipal User currentUser) {
        
        // 调用 Service 层 (它会处理鉴权和文件加载)
        StreamResourceDto streamedResource = resourceService.getResourceStream(resourceId, currentUser);

        // 构建响应
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + streamedResource.getFilename() + "\"")
                .body(streamedResource.getResourceFile());
    }
}