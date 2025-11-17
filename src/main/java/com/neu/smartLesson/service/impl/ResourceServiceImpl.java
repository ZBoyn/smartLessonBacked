package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.ResourceResponseDto;
import com.neu.smartLesson.dto.StreamResourceDto;
import com.neu.smartLesson.exception.RegistrationException;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.exception.UnauthorizedException;
import com.neu.smartLesson.mapper.EnrollmentMapper;
import com.neu.smartLesson.mapper.KnowledgePointMapper;
import com.neu.smartLesson.mapper.ResourceKnowledgePointMapper;
import com.neu.smartLesson.mapper.ResourceMapper;
import com.neu.smartLesson.model.*;
import com.neu.smartLesson.service.FileStorageService;
import com.neu.smartLesson.service.ResourceService;
import com.neu.smartLesson.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceKnowledgePointMapper rkMapper;

    // 用于鉴权
    @Autowired
    private CourseService courseService;

    @Autowired
    private KnowledgePointMapper kpMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Override
    @Transactional // 必须是事务性的
    public ResourceResponseDto uploadResource(Integer courseId, User teacher, MultipartFile file, String title, List<Integer> knowledgePointIds) {

        Integer teacherId = teacher.getUserId();

        // 检查教师是否有权操作此课程
        courseService.checkCourseOwnership(courseId, teacherId);

        // 检查所有 KpId 都属于这个课程
        if (knowledgePointIds == null || knowledgePointIds.isEmpty()) {
            throw new RegistrationException("必须至少关联一个知识点。");
        }
        for (Integer kpId : knowledgePointIds) {
            KnowledgePoint kp = kpMapper.findKnowledgePointById(kpId)
                    .orElseThrow(() -> new RegistrationException("知识点 (ID: " + kpId + ") 不存在。"));
            if (!kp.getCourseId().equals(courseId)) {
                throw new RegistrationException("知识点 (ID: " + kpId + ") 不属于此课程 (ID: " + courseId + ")。");
            }
        }

        // 【文件I/O】存储物理文件
        String safeFilename = fileStorageService.storeFile(file);
        String resourceType = fileStorageService.getResourceType(file);

        // 【DB】创建 Resource POJO
        Resource resource = Resource.builder()
                .courseId(courseId)
                .title(title)
                .resourceType(resourceType)
                .filePath(safeFilename)
                .uploadedById(teacherId)
                .build();

        // 【DB】插入 Resources 表
        resourceMapper.insertResource(resource); // resourceId 会被回填

        // 【DB】批量插入 ResourceKnowledgePoints 表
        rkMapper.insertBatch(resource.getResourceId(), knowledgePointIds);

        // 返回响应 DTO
        return ResourceResponseDto.builder()
                .resourceId(resource.getResourceId())
                .courseId(resource.getCourseId())
                .title(resource.getTitle())
                .resourceType(resource.getResourceType())
                .filePath("/resources/" + resource.getResourceId())
                .uploadedById(teacherId)
                .knowledgePointIds(knowledgePointIds)
                .build();
    }

    @Override
    public List<ResourceResponseDto> getResourcesForCourse(Integer courseId, Integer teacherId) {
        // 鉴权
        courseService.checkCourseOwnership(courseId, teacherId);

        // 查询
        List<Resource> resources = resourceMapper.findResourcesByCourseId(courseId);

        // 转换 DTO
        // (V2.0: 我们需要一个 ResourceDtoMapper)
        // (R1.0: 快速实现)
        return resources.stream().map(res -> ResourceResponseDto.builder()
                .resourceId(res.getResourceId())
                .courseId(res.getCourseId())
                .title(res.getTitle())
                .resourceType(res.getResourceType())
                .filePath("/resources/" + res.getResourceId()) // 构造 API URL
                .uploadedById(res.getUploadedById())
                // (V2.0: 这里没有返回 KpIds, GET 列表时需要 N+1 查询, 暂 R1.0 忽略)
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * DELETE
     */
    @Override
    @Transactional
    public void deleteResource(Integer resourceId, Integer teacherId) {
        // 1. 鉴权
        Resource resource = resourceMapper.findResourceById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("资源 (ID: " + resourceId + ") 未找到。"));
        courseService.checkCourseOwnership(resource.getCourseId(), teacherId);

        // 2. (V2.0: 删除物理文件, fileStorageService.deleteFile(resource.getFilePath()))
        // 3. (V2.0: 级联删除 rkMapper.deleteByResourceId(resourceId))

        // 4. 【DB】删除
        resourceMapper.deleteResourceById(resourceId);
    }

    /**
     * 实现 getResourceStream, 包含完整的安全检查
     */
    @Override
    public StreamResourceDto getResourceStream(Integer resourceId, User currentUser) {

        // 获取资源元数据
        Resource resource = resourceMapper.findResourceById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("资源 (ID: " + resourceId + ") 未找到。"));

        Integer courseId = resource.getCourseId();

        // 核心安全检查
        if (currentUser.getRole() == Role.TEACHER) {
            // 教师：必须是课程的创建者
            courseService.checkCourseOwnership(courseId, currentUser.getUserId());
        }
        else if (currentUser.getRole() == Role.STUDENT) {
            // 学生：必须已注册该课程
            Integer enrollmentCount = enrollmentMapper.checkEnrollment(currentUser.getUserId(), courseId);
            if (enrollmentCount == 0) {
                throw new UnauthorizedException("您 (学生 ID: " + currentUser.getUserId() + ") 未注册此课程 (Course ID: " + courseId + ")，无权访问此资源。");
            }
        }
        else {
            // 其他角色 (如果未来有的话)
            throw new UnauthorizedException("您的角色无权访问此资源。");
        }

        // 3. 【鉴权通过】加载物理文件
        String filename = resource.getFilePath();
        org.springframework.core.io.Resource file = fileStorageService.loadFileAsResource(filename);

        // 4. 返回 DTO
        return new StreamResourceDto(file, filename);
    }
}