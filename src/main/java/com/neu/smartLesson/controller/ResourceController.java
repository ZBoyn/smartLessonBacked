package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.ResourceResponseDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 上传资源 (视频/PDF) 并将其关联到知识点
     * POST /teacher/courses/{courseId}/resources
     *
     * @param courseId 课程ID
     * @param file 必须的文件 (e.g., 'intro.mp4')
     * @param title 资源标题 (e.g., "SQL Joins 详解")
     * @param knowledgePointIds 关联的知识点ID (e.g., [1, 5])
     * @return 创建的资源信息
     */
    @PostMapping(path = "/courses/{courseId}/resources", consumes = "multipart/form-data")
    public ResponseEntity<ResourceResponseDto> uploadResource(
            @PathVariable Integer courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("knowledgePointIds") List<Integer> knowledgePointIds,
            @AuthenticationPrincipal User currentUser) {

        ResourceResponseDto response = resourceService.uploadResource(
                courseId,
                currentUser,
                file,
                title,
                knowledgePointIds
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 获取课程下的所有资源 (GET 列表)
     * GET /teacher/courses/{courseId}/resources
     */
    @GetMapping("/courses/{courseId}/resources")
    public ResponseEntity<List<ResourceResponseDto>> getResources(
            @PathVariable Integer courseId, @AuthenticationPrincipal User currentUser) {
        
        List<ResourceResponseDto> resources = resourceService.getResourcesForCourse(courseId, currentUser.getUserId());
        return ResponseEntity.ok(resources);
    }

    /**
     * 删除一个资源 (DELETE)
     * DELETE /teacher/resources/{resourceId}
     */
    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Integer resourceId, @AuthenticationPrincipal User currentUser) {

        resourceService.deleteResource(resourceId, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}