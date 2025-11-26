package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.AssessmentResponseDto;
import com.neu.smartLesson.dto.CreateAssessmentRequestDto;
import com.neu.smartLesson.dto.PublishAssessmentRequestDto;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.mapper.*;
import com.neu.smartLesson.model.Assessment;
import com.neu.smartLesson.model.CourseClass;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.AssessmentService;
import com.neu.smartLesson.service.CourseService;
import com.neu.smartLesson.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neu.smartLesson.dto.QuestionFullDto;
import com.neu.smartLesson.dto.QuestionOptionDto;
import com.neu.smartLesson.dto.StudentAssessmentDetailDto;
import com.neu.smartLesson.model.Question;
import com.neu.smartLesson.model.QuestionOption;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentServiceImpl implements AssessmentService {

        @Autowired
        private AssessmentMapper assessmentMapper;
        @Autowired
        private AssessmentQuestionMapper aqMapper;
        @Autowired
        private ClassMapper classMapper;
        @Autowired
        private CourseService courseService;
        @Autowired
        private KnowledgeGraphService kgService; // 用于题目鉴权
        @Autowired
        private QuestionMapper questionMapper;
        @Autowired
        private QuestionOptionMapper questionOptionMapper;

        @Override
        @Transactional
        public AssessmentResponseDto createAssessment(CreateAssessmentRequestDto dto, Integer classId, User teacher) {
                // 1. 鉴权：检查教师是否拥有此班级 (通过课程)
                CourseClass courseClass = checkClassOwnership(classId, teacher.getUserId());
                Integer courseId = courseClass.getCourseId();

                // 2. 鉴权：检查所有 QuestionId 是否都属于此课程
                // (复用 Hotfix 4.0 的辅助方法)
                kgService.checkKnowledgePointsExist(dto.getQuestionIds(), courseId); // R1.0 假设 question Kp 检查足够

                // 3. 插入 Assessment (状态默认为 'draft')
                Assessment assessment = Assessment.builder()
                                .classId(classId)
                                .title(dto.getTitle())
                                .assessmentType(dto.getAssessmentType())
                                .createdById(teacher.getUserId())
                                .build();
                assessmentMapper.insertAssessment(assessment); // assessmentId 已回填

                Integer newAssessmentId = assessment.getAssessmentId();

                // 4. 批量插入 AssessmentQuestions
                aqMapper.insertBatchQuestions(newAssessmentId, dto.getQuestionIds());

                // 5. 返回 DTO
                return AssessmentResponseDto.builder()
                                .assessmentId(newAssessmentId)
                                .classId(classId)
                                .title(assessment.getTitle())
                                .status(assessment.getStatus()) // 'draft'
                                .questionIds(dto.getQuestionIds())
                                .build();
        }

        @Override
        @Transactional
        public AssessmentResponseDto publishAssessment(PublishAssessmentRequestDto dto, Integer assessmentId,
                        User teacher) {

                // 1. 鉴权：检查教师是否拥有此测评 (通过班级 -> 课程)
                Assessment assessment = checkAssessmentOwnership(assessmentId, teacher.getUserId());

                // 2. 更新测评状态
                assessment.setStatus("published");
                assessment.setStartTime(dto.getStartTime());
                assessment.setEndTime(dto.getEndTime());

                assessmentMapper.updateAssessmentStatus(assessment);

                // 3. 返回更新后的 DTO
                return AssessmentResponseDto.builder()
                                .assessmentId(assessmentId)
                                .classId(assessment.getClassId())
                                .title(assessment.getTitle())
                                .status(assessment.getStatus()) // 'published'
                                .startTime(assessment.getStartTime())
                                .endTime(assessment.getEndTime())
                                .questionIds(aqMapper.findQuestionIdsByAssessmentId(assessmentId)) // 重新查询
                                .build();
        }

        @Override
        public List<AssessmentResponseDto> getAssessmentsByClass(Integer classId, User teacher) {
                // 1. 鉴权
                checkClassOwnership(classId, teacher.getUserId());

                // 2. Fetch
                List<Assessment> assessments = assessmentMapper.findByClassId(classId);

                // 3. Convert
                return assessments.stream().map(a -> AssessmentResponseDto.builder()
                                .assessmentId(a.getAssessmentId())
                                .classId(a.getClassId())
                                .title(a.getTitle())
                                .assessmentType(a.getAssessmentType())
                                .status(a.getStatus())
                                .startTime(a.getStartTime())
                                .endTime(a.getEndTime())
                                .build()).collect(Collectors.toList());
        }

        @Override
        public StudentAssessmentDetailDto getAssessmentDetailForStudent(Integer assessmentId, User student) {
                // 1. Fetch assessment
                Assessment assessment = assessmentMapper.findAssessmentById(assessmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

                // 2. TODO: Check student enrollment (skipping for prototype)

                // 3. Fetch questions
                List<Integer> qIds = aqMapper.findQuestionIdsByAssessmentId(assessmentId);
                List<QuestionFullDto> questions = new java.util.ArrayList<>();

                for (Integer qId : qIds) {
                        Question q = questionMapper.findQuestionById(qId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
                        List<QuestionOption> options = questionOptionMapper.findOptionsByQuestionId(qId);

                        List<QuestionOptionDto> optionDtos = options.stream()
                                        .map(o -> QuestionOptionDto.builder()
                                                        .optionId(o.getOptionId())
                                                        .optionText(o.getOptionText())
                                                        // Don't send isCorrect to student!
                                                        .build())
                                        .collect(Collectors.toList());

                        questions.add(QuestionFullDto.builder()
                                        .questionId(q.getQuestionId())
                                        .prompt(q.getPrompt())
                                        .questionType(q.getQuestionType().name()) // Enum to string
                                        .difficulty(q.getDifficulty().name())
                                        .supportsAiGrading(q.getSupportsAiGrading())
                                        .options(optionDtos)
                                        .build());
                }

                return StudentAssessmentDetailDto.builder()
                                .assessmentId(assessmentId)
                                .title(assessment.getTitle())
                                .status(assessment.getStatus())
                                .startTime(assessment.getStartTime() != null ? assessment.getStartTime().toString()
                                                : null)
                                .endTime(assessment.getEndTime() != null ? assessment.getEndTime().toString() : null)
                                .questions(questions)
                                .build();
        }

        // =================================================================
        // 辅助方法 (用于鉴权)
        // =================================================================

        // 辅助：检查教师是否拥有该班级
        private CourseClass checkClassOwnership(Integer classId, Integer teacherId) {
                CourseClass courseClass = classMapper.findClassById(classId) // (需要添加到 ClassMapper)
                                .orElseThrow(() -> new ResourceNotFoundException("班级 (ID: " + classId + ") 未找到。"));

                // 检查该班级所属的课程是否属于该教师
                courseService.checkCourseOwnership(courseClass.getCourseId(), teacherId);
                return courseClass;
        }

        // 辅助：检查教师是否拥有该测评
        private Assessment checkAssessmentOwnership(Integer assessmentId, Integer teacherId) {
                Assessment assessment = assessmentMapper.findAssessmentById(assessmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("测评 (ID: " + assessmentId + ") 未找到。"));

                // 检查该测评所属的班级是否属于该教师
                checkClassOwnership(assessment.getClassId(), teacherId);
                return assessment;
        }
}