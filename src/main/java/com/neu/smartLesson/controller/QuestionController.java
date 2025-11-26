package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.QuestionFullDto;
import com.neu.smartLesson.dto.QuestionSimpleDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher") // 教师管理题库
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * (Create) 创建新题目 (US11, 12, 13)
     * POST /teacher/courses/{courseId}/questions
     */
    @PostMapping("/courses/{courseId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuestionFullDto> createQuestion(
            @PathVariable Integer courseId,
            @Valid @RequestBody QuestionFullDto dto,
            @AuthenticationPrincipal User currentUser) {

        QuestionFullDto createdQuestion = questionService.createQuestion(dto, courseId, currentUser);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    /**
     * (Read-List) 获取课程题库列表
     * GET /teacher/courses/{courseId}/questions
     */
    @GetMapping("/courses/{courseId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuestionSimpleDto>> getQuestionsForCourse(
            @PathVariable Integer courseId,
            @AuthenticationPrincipal User currentUser) {

        List<QuestionSimpleDto> questions = questionService.getQuestionsForCourse(courseId, currentUser);
        return ResponseEntity.ok(questions);
    }

    /**
     * (Read-Detail) 获取单个题目详情
     * (允许教师和已注册的学生访问)
     * GET /teacher/questions/{questionId} (V2.0: 也许 /questions/{id} 更合适)
     */
    @GetMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<QuestionFullDto> getQuestionById(
            @PathVariable Integer questionId,
            @AuthenticationPrincipal User currentUser) {

        QuestionFullDto question = questionService.getQuestionById(questionId, currentUser);
        return ResponseEntity.ok(question);
    }

    /**
     * (Update) 更新题目
     * PUT /teacher/questions/{questionId}
     */
    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuestionFullDto> updateQuestion(
            @PathVariable Integer questionId,
            @Valid @RequestBody QuestionFullDto dto,
            @AuthenticationPrincipal User currentUser) {

        QuestionFullDto updatedQuestion = questionService.updateQuestion(questionId, dto, currentUser);
        return ResponseEntity.ok(updatedQuestion);
    }

    /**
     * (Delete) 删除题目
     * DELETE /teacher/questions/{questionId}
     */
    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Integer questionId,
            @AuthenticationPrincipal User currentUser) {

        questionService.deleteQuestion(questionId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * (Smart Assemble) 智能组卷
     * POST /teacher/questions/generate
     */
    @PostMapping("/questions/generate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<com.neu.smartLesson.dto.AssessmentResponseDto> generatePaper(
            @RequestBody com.neu.smartLesson.dto.GeneratePaperRequestDto dto,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(questionService.generatePaper(dto, currentUser));
    }
}