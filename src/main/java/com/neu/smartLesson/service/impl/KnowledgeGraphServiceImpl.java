package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.CreateRelationRequestDto;
import com.neu.smartLesson.dto.GraphDto;
import com.neu.smartLesson.dto.KnowledgePointDto;
import com.neu.smartLesson.dto.RelationResponseDto;
import com.neu.smartLesson.exception.RegistrationException;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.mapper.*;
import com.neu.smartLesson.model.Course;
import com.neu.smartLesson.model.KnowledgePoint;
import com.neu.smartLesson.model.KnowledgeRelation;
import com.neu.smartLesson.service.CourseService;
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
    private CourseService courseService;

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

        // 在删除“节点”之前，必须先删除所有连接到它的“边”
        // System.out.println("正在级联删除 KpId: " + kpId + " 的相关“边”...");
        krMapper.deleteRelationsByKpId(kpId);

        // 删除“节点”
        kpMapper.deleteKnowledgePointById(kpId);
    }


    @Override
    @Transactional
    public RelationResponseDto createRelation(CreateRelationRequestDto dto, Integer courseId, Integer teacherId) {

        // 检查教师是否有权操作此课程 (父课程)
        checkCourseOwnership(courseId, teacherId);

        // 检查源节点和目标节点都属于此课程
        KnowledgePoint sourceKp = checkKnowledgePointOwnership(dto.getSourceKpId(), teacherId);
        KnowledgePoint targetKp = checkKnowledgePointOwnership(dto.getTargetKpId(), teacherId);

        // 确保源节点和目标节点都属于同一个课程
        if (!Objects.equals(sourceKp.getCourseId(), targetKp.getCourseId())) {
            throw new RegistrationException("错误：源节点和目标节点不属于同一个课程。");
        }
        // 并且它们都属于我们正在操作的这个 courseId
        if (!Objects.equals(sourceKp.getCourseId(), courseId)) {
            throw new RegistrationException("错误：这些知识点不属于您正在操作的课程 (ID: " + courseId + ")");
        }

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

        return GraphDto.builder()
                .nodes(kDtoMapper.toDtoList(nodes))
                .edges(krDtoMapper.toDtoList(edges))
                .build();
    }

    /**
     * 辅助方法：检查一个课程是否属于某个教师
     */
    private Course checkCourseOwnership(Integer courseId, Integer teacherId) {
        // 将鉴权委托给 CourseService
        return courseService.checkCourseOwnership(courseId, teacherId);
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