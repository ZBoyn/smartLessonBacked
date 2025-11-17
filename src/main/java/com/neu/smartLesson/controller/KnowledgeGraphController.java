package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.CreateRelationRequestDto;
import com.neu.smartLesson.dto.GraphDto;
import com.neu.smartLesson.dto.KnowledgePointDto;
import com.neu.smartLesson.dto.RelationResponseDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.KnowledgeGraphService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class KnowledgeGraphController {

    @Autowired
    private KnowledgeGraphService kgService;

    /**
     * POST /teacher/courses/{courseId}/knowledge-points
     * (US-T07) 在课程中创建新知识点 (节点)
     */
    @PostMapping("/courses/{courseId}/knowledge-points")
    public ResponseEntity<KnowledgePointDto> createKnowledgePoint(
            @PathVariable Integer courseId,
            @Valid @RequestBody KnowledgePointDto requestDto) {

        Integer teacherId = getCurrentUserId();
        KnowledgePointDto createdKp = kgService.createKnowledgePoint(requestDto, courseId, teacherId);
        return new ResponseEntity<>(createdKp, HttpStatus.CREATED);
    }

    /**
     * PUT /teacher/knowledge-points/{kpId}
     * (US-T10) 更新一个知识点 (节点)
     */
    @PutMapping("/knowledge-points/{kpId}")
    public ResponseEntity<KnowledgePointDto> updateKnowledgePoint(
            @PathVariable Integer kpId,
            @Valid @RequestBody KnowledgePointDto requestDto) {

        Integer teacherId = getCurrentUserId();
        KnowledgePointDto updatedKp = kgService.updateKnowledgePoint(kpId, requestDto, teacherId);
        return ResponseEntity.ok(updatedKp);
    }

    /**
     * DELETE /teacher/knowledge-points/{kpId}
     * (US-T10) 删除一个知识点 (节点)
     */
    @DeleteMapping("/knowledge-points/{kpId}")
    public ResponseEntity<Void> deleteKnowledgePoint(@PathVariable Integer kpId) {

        Integer teacherId = getCurrentUserId();
        kgService.deleteKnowledgePoint(kpId, teacherId);
        return ResponseEntity.noContent().build(); // 返回 204 No Content
    }

    /**
     * POST /teacher/courses/{courseId}/knowledge-relations
     * (US-T08) 在课程中创建新关系 (边)
     */
    @PostMapping("/courses/{courseId}/knowledge-relations")
    public ResponseEntity<RelationResponseDto> createRelation(
            @PathVariable Integer courseId,
            @Valid @RequestBody CreateRelationRequestDto requestDto) {

        Integer teacherId = getCurrentUserId();
        RelationResponseDto createdRelation = kgService.createRelation(requestDto, courseId, teacherId);
        return new ResponseEntity<>(createdRelation, HttpStatus.CREATED);
    }

    /**
     * DELETE /teacher/knowledge-relations/{relationId}
     * (US-T10) 删除一个关系 (边)
     */
    @DeleteMapping("/knowledge-relations/{relationId}")
    public ResponseEntity<Void> deleteRelation(@PathVariable Integer relationId) {

        Integer teacherId = getCurrentUserId();
        kgService.deleteRelation(relationId, teacherId);
        return ResponseEntity.noContent().build(); // 返回 204
    }

    @GetMapping("/courses/{courseId}/graph")
    public ResponseEntity<GraphDto> getGraphForCourse(@PathVariable Integer courseId) {

        Integer teacherId = getCurrentUserId();
        GraphDto graph = kgService.getGraphForCourse(courseId, teacherId);
        return ResponseEntity.ok(graph);
    }

    /**
     * 辅助方法: 从 Spring Security 上下文中获取用户ID
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getUserId();
    }
}