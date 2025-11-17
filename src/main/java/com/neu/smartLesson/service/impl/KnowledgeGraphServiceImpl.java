package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.CreateRelationRequestDto;
import com.neu.smartLesson.dto.GraphDto;
import com.neu.smartLesson.dto.KnowledgePointDto;
import com.neu.smartLesson.dto.RelationResponseDto;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.exception.UnauthorizedException;
import com.neu.smartLesson.mapper.*;
import com.neu.smartLesson.model.Course;
import com.neu.smartLesson.model.KnowledgePoint;
import com.neu.smartLesson.model.KnowledgeRelation;
import com.neu.smartLesson.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    @Autowired
    private KnowledgePointMapper kpMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private KnowledgePointDtoMapper kDtoMapper;

    @Autowired
    private KnowledgeRelationMapper krMapper;

    @Autowired
    private KnowledgeRelationDtoMapper krDtoMapper;


    @Override
    @Transactional // 确保操作的原子性
    public KnowledgePointDto createKnowledgePoint(KnowledgePointDto dto, Integer courseId, Integer teacherId) {

        checkCourseOwnership(courseId, teacherId);

        KnowledgePoint kp = kDtoMapper.toModel(dto, courseId);

        kpMapper.insertKnowledgePoint(kp); // kpId 会被回填

        return kDtoMapper.toDto(kp);
    }

    @Override
    @Transactional
    public KnowledgePointDto updateKnowledgePoint(Integer kpId, KnowledgePointDto dto, Integer teacherId) {

        // 查找知识点并鉴权
        KnowledgePoint existingKp = checkKnowledgePointOwnership(kpId, teacherId);

        // 更新模型
        existingKp.setName(dto.getName());
        existingKp.setDescription(dto.getDescription());

        // 更新数据库
        kpMapper.updateKnowledgePoint(existingKp);

        // 返回 DTO
        return kDtoMapper.toDto(existingKp);
    }

    @Override
    @Transactional
    public void deleteKnowledgePoint(Integer kpId, Integer teacherId) {

        // 查找知识点并鉴权
        checkKnowledgePointOwnership(kpId, teacherId);

        // (V2.0: 在删除节点前，我们应该先删除所有与它相关的“边” (Relations))
        // (R1.0: 我们先实现基础删除)

        // 删除数据库中的知识点
        kpMapper.deleteKnowledgePointById(kpId);

    }


    @Override
    @Transactional
    public RelationResponseDto createRelation(CreateRelationRequestDto dto, Integer courseId, Integer teacherId) {

        // 检查教师是否有权操作此课程 (父课程)
        checkCourseOwnership(courseId, teacherId);

        // 检查源节点和目标节点都属于此课程
        checkKnowledgePointOwnership(dto.getSourceKpId(), teacherId);
        checkKnowledgePointOwnership(dto.getTargetKpId(), teacherId);

        // (V2.0: 还应检查 source 和 target 是否属于同一个 courseId, 暂 R1.0 忽略)

        // DTO -> Model
        KnowledgeRelation relation = KnowledgeRelation.builder()
                .sourceKpId(dto.getSourceKpId())
                .targetKpId(dto.getTargetKpId())
                .relationType(dto.getRelationType())
                .build();

        // 插入数据库
        krMapper.insertRelation(relation); // relationId 会被回填

        // Model -> DTO
        return krDtoMapper.toDto(relation);
    }

    @Override
    @Transactional
    public void deleteRelation(Integer relationId, Integer teacherId) {

        // 查找 "边"
        KnowledgeRelation relation = krMapper.findRelationById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("关系 (Relation) 未找到, ID: " + relationId));

        // 检查教师是否有权操作这条 "边" (通过检查其源节点的所有权)
        checkKnowledgePointOwnership(relation.getSourceKpId(), teacherId);

        // 删除
        krMapper.deleteRelationById(relationId);
    }

    @Override
    public GraphDto getGraphForCourse(Integer courseId, Integer teacherId) {

        checkCourseOwnership(courseId, teacherId);

        // 获取所有节点 (Nodes)
        List<KnowledgePoint> nodes = kpMapper.findKnowledgePointsByCourseId(courseId);

        // 获取所有边 (Edges)
        List<KnowledgeRelation> edges = krMapper.findRelationsByCourseId(courseId);

        // 转换 DTO
        GraphDto graph = GraphDto.builder()
                .nodes(kDtoMapper.toDtoList(nodes))
                .edges(krDtoMapper.toDtoList(edges))
                .build();

        return graph;
    }

    /**
     * 辅助方法：检查一个课程是否属于某个教师
     */
    private Course checkCourseOwnership(Integer courseId, Integer teacherId) {
        Course course = courseMapper.findCourseById(courseId) // 我们需要在 CourseMapper 中添加 findCourseById
                .orElseThrow(() -> new ResourceNotFoundException("课程 (Course) 未找到, ID: " + courseId));

        if (!Objects.equals(course.getCreatorId(), teacherId)) {
            throw new UnauthorizedException("您 (ID: " + teacherId + ") 无权操作此课程 (ID: " + courseId + ")");
        }
        return course;
    }

    /**
     * 辅助方法：检查一个知识点是否属于某个教师 (通过其所属的课程)
     */
    private KnowledgePoint checkKnowledgePointOwnership(Integer kpId, Integer teacherId) {
        KnowledgePoint kp = kpMapper.findKnowledgePointById(kpId)
                .orElseThrow(() -> new ResourceNotFoundException("知识点 (KnowledgePoint) 未找到, ID: " + kpId));

        // 检查该知识点所属的课程是否属于该教师
        checkCourseOwnership(kp.getCourseId(), teacherId);

        return kp;
    }
}