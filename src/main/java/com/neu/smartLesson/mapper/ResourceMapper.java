package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ResourceMapper {

    /**
     * 插入一条新的资源元数据
     * @param resource Resource POJO
     */
    void insertResource(Resource resource);

    /**
     * 根据ID查找资源 (用于 FileStreamController)
     * @param resourceId 资源ID
     */
    Optional<Resource> findResourceById(@Param("resourceId") Integer resourceId);

    /**
     * 查询课程下的所有资源 (GET)
     * @param courseId 课程ID
     */
    List<Resource> findResourcesByCourseId(@Param("courseId") Integer courseId);

    /**
     * 删除一个资源 (DELETE)
     * @param resourceId 资源ID
     */
    void deleteResourceById(@Param("resourceId") Integer resourceId);
}