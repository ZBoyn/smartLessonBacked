package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.AssessmentResponseDto;
import com.neu.smartLesson.dto.CreateAssessmentRequestDto;
import com.neu.smartLesson.dto.PublishAssessmentRequestDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    /**
     * (US15, 16) 教师为班级“组卷” (创建测评草稿)
     * POST /teacher/classes/{classId}/assessments
     */
    @PostMapping("/classes/{classId}/assessments")
    public ResponseEntity<AssessmentResponseDto> createAssessment(
            @PathVariable Integer classId,
            @Valid @RequestBody CreateAssessmentRequestDto dto,
            @AuthenticationPrincipal User currentUser) {

        AssessmentResponseDto response = assessmentService.createAssessment(dto, classId, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * (US18) 教师“发布”测评
     * POST /teacher/assessments/{assessmentId}/publish
     */
    @PostMapping("/assessments/{assessmentId}/publish")
    public ResponseEntity<AssessmentResponseDto> publishAssessment(
            @PathVariable Integer assessmentId,
            @Valid @RequestBody PublishAssessmentRequestDto dto,
            @AuthenticationPrincipal User currentUser) {

        AssessmentResponseDto response = assessmentService.publishAssessment(dto, assessmentId, currentUser);
        return ResponseEntity.ok(response);
    }
}