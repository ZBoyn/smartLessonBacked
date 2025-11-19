package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.ResourceResponseDto;
import com.neu.smartLesson.dto.SubmissionResponseDto;
import com.neu.smartLesson.dto.SubmitAnswersRequestDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/student/assessments")
@PreAuthorize("hasRole('STUDENT')")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    /**
     * 保存答案 (自动保存/暂存)
     * POST /student/assessments/{assessmentId}/answers
     */
    @PostMapping("/{assessmentId}/answers")
    public ResponseEntity<Void> saveAnswers(
            @PathVariable Integer assessmentId,
            @RequestBody SubmitAnswersRequestDto dto,
            @AuthenticationPrincipal User student) {

        submissionService.saveAnswers(assessmentId, dto, student);
        return ResponseEntity.ok().build();
    }

    /**
     * 上传报告题文件 (Task 3 核心功能)
     * POST /student/assessments/{assessmentId}/answers/{questionId}/upload
     */
    @PostMapping(path = "/{assessmentId}/answers/{questionId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResourceResponseDto> uploadAnswerFile(
            @PathVariable Integer assessmentId,
            @PathVariable Integer questionId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User student) {

        ResourceResponseDto response = submissionService.uploadAnswerFile(assessmentId, questionId, file, student);
        return ResponseEntity.ok(response);
    }

    /**
     * 最终提交
     * POST /student/assessments/{assessmentId}/submit
     */
    @PostMapping("/{assessmentId}/submit")
    public ResponseEntity<SubmissionResponseDto> submitAssessment(
            @PathVariable Integer assessmentId,
            @AuthenticationPrincipal User student) {

        SubmissionResponseDto response = submissionService.finalizeSubmission(assessmentId, student);
        return ResponseEntity.ok(response);
    }
}