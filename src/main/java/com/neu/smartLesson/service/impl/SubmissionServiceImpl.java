package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.ResourceResponseDto;
import com.neu.smartLesson.dto.SubmissionResponseDto;
import com.neu.smartLesson.dto.SubmitAnswersRequestDto;
import com.neu.smartLesson.exception.RegistrationException;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.exception.UnauthorizedException;
import com.neu.smartLesson.mapper.*;
import com.neu.smartLesson.model.*;
import com.neu.smartLesson.service.FileStorageService;
import com.neu.smartLesson.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired private SubmissionMapper submissionMapper;
    @Autowired private AnswerMapper answerMapper;
    @Autowired private AssessmentMapper assessmentMapper;
    @Autowired private EnrollmentMapper enrollmentMapper;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private QuestionMapper questionMapper;

    // 【新增】注入 ClassMapper 用于查找班级信息
    @Autowired private ClassMapper classMapper;

    @Override
    @Transactional
    public void saveAnswers(Integer assessmentId, SubmitAnswersRequestDto dto, User student) {
        // 1. 获取或创建 Submission
        Submission submission = getOrCreateSubmission(assessmentId, student);

        if ("submitted".equals(submission.getStatus())) {
            throw new RegistrationException("作业已提交，无法修改。");
        }

        // 2. 遍历保存答案
        for (SubmitAnswersRequestDto.SingleAnswerDto ansDto : dto.getAnswers()) {
            saveSingleAnswer(submission.getSubmissionId(), ansDto);
        }
    }

    @Override
    @Transactional
    public ResourceResponseDto uploadAnswerFile(Integer assessmentId, Integer questionId, MultipartFile file, User student) {
        // 1. 获取 Submission
        Submission submission = getOrCreateSubmission(assessmentId, student);

        // 2. 检查题目是否是报告题 (Task 3)
        Question question = questionMapper.findQuestionById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        // (这里可以加校验: question.getType() == 'report')

        // 3. 存储文件
        String safeFilename = fileStorageService.storeFile(file);

        // 4. 保存答案记录
        Answer answer = Answer.builder()
                .submissionId(submission.getSubmissionId())
                .questionId(questionId)
                .filePath(safeFilename)
                .build();

        answerMapper.upsertAnswer(answer); // answerId 回填

        return ResourceResponseDto.builder()
                .filePath("/resources/answers/" + answer.getAnswerId()) // V2.0: 下载答案的接口
                .title(file.getOriginalFilename())
                .build();
    }

    @Override
    @Transactional
    public SubmissionResponseDto finalizeSubmission(Integer assessmentId, User student) {
        Submission submission = getOrCreateSubmission(assessmentId, student);

        // 更新状态
        submission.setStatus("submitted");
        submission.setSubmittedAt(Timestamp.from(Instant.now()));
        submissionMapper.updateSubmission(submission);

        // (V2.0: 这里可以触发异步事件 -> AI 自动批改)

        return SubmissionResponseDto.builder()
                .submissionId(submission.getSubmissionId())
                .assessmentId(assessmentId)
                .status("submitted")
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    // === 辅助方法 ===

    private Submission getOrCreateSubmission(Integer assessmentId, User student) {
        // 1. 检查 Assessment 是否存在
        Assessment assessment = assessmentMapper.findAssessmentById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("测评不存在"));

        // 2. 【修复】检查是否已注册
        // 逻辑：Assessment -> Class -> Course -> Check Enrollment
        CourseClass courseClass = classMapper.findClassById(assessment.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("该测评所属的班级不存在"));

        Integer courseId = courseClass.getCourseId(); // 获取 Course ID

        // 使用 enrollmentMapper 检查学生是否在课程中 (实际上是检查该课程下的任何班级)
        Integer enrolled = enrollmentMapper.checkEnrollment(student.getUserId(), courseId);

        if (enrolled == 0) {
            throw new UnauthorizedException("您未注册此课程，无法提交作业。");
        }

        // 3. 查找现有提交
        return submissionMapper.findByAssessmentIdAndStudentId(assessmentId, student.getUserId())
                .orElseGet(() -> {
                    // 创建新提交
                    Submission newSub = Submission.builder()
                            .assessmentId(assessmentId)
                            .studentId(student.getUserId())
                            .status("in_progress")
                            .build();
                    submissionMapper.insertSubmission(newSub);
                    return newSub;
                });
    }

    private void saveSingleAnswer(Integer submissionId, SubmitAnswersRequestDto.SingleAnswerDto ansDto) {
        // 保存 Answer 主体
        Answer answer = Answer.builder()
                .submissionId(submissionId)
                .questionId(ansDto.getQuestionId())
                .answerText(ansDto.getAnswerText())
                .build();
        answerMapper.upsertAnswer(answer);

        // 如果是选择题，保存选项
        if (ansDto.getSelectedOptionIds() != null && !ansDto.getSelectedOptionIds().isEmpty()) {
            // 先清空旧选项
            answerMapper.deleteAnswerOptionsByAnswerId(answer.getAnswerId());

            List<AnswerOption> options = new ArrayList<>();
            for (Integer optId : ansDto.getSelectedOptionIds()) {
                options.add(AnswerOption.builder()
                        .answerId(answer.getAnswerId())
                        .optionId(optId)
                        .build());
            }
            answerMapper.insertBatchAnswerOptions(options);
        }
    }
}