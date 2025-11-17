package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.ResourceResponseDto;
import com.neu.smartLesson.dto.StreamResourceDto;
import com.neu.smartLesson.model.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ResourceService {

    /**
     * 教师上传资源并关联到知识点
     * @param courseId 课程ID
     * @param teacher 上传资源的教师用户
     * @param file 上传的文件
     * @param title 资源标题
     * @param knowledgePointIds 要关联的知识点ID列表
     * @return 创建的资源 DTO
     */
    ResourceResponseDto uploadResource(
            Integer courseId,
            User teacher,
            MultipartFile file,
            String title,
            List<Integer> knowledgePointIds
    );

    /**
     * 获取课程下的所有资源 (GET)
     * @param courseId 课程ID
     * @param teacherId 教师ID (鉴权)
     */
    List<ResourceResponseDto> getResourcesForCourse(Integer courseId, Integer teacherId);

    /**
     * 删除一个资源 (DELETE)
     * @param resourceId 资源ID
     * @param teacherId 教师ID (鉴权)
     */
    void deleteResource(Integer resourceId, Integer teacherId);

    /**
     * 统一处理资源加载和鉴权
     *
     * @param resourceId 资源ID
     * @param currentUser 尝试访问的登录用户 (可能是教师或学生)
     * @return 包含文件流和标题的 DTO
     */
    StreamResourceDto getResourceStream(Integer resourceId, User currentUser);
}