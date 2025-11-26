package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.StudentAssessmentDetailDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/assessments")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @GetMapping("/{assessmentId}")
    public ResponseEntity<StudentAssessmentDetailDto> getAssessmentDetail(
            @PathVariable Integer assessmentId,
            @AuthenticationPrincipal User currentUser) {
        
        StudentAssessmentDetailDto dto = assessmentService.getAssessmentDetailForStudent(assessmentId, currentUser);
        return ResponseEntity.ok(dto);
    }
}

