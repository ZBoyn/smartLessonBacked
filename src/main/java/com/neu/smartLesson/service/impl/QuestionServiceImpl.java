package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.QuestionFullDto;
import com.neu.smartLesson.dto.QuestionOptionDto;
import com.neu.smartLesson.dto.QuestionSimpleDto;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.exception.UnauthorizedException;
import com.neu.smartLesson.mapper.*;
import com.neu.smartLesson.model.*;
import com.neu.smartLesson.service.CourseService;
import com.neu.smartLesson.service.KnowledgeGraphService;
import com.neu.smartLesson.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors; // Added Import

import com.neu.smartLesson.dto.GeneratePaperRequestDto;
import com.neu.smartLesson.dto.AssessmentResponseDto;
import com.neu.smartLesson.model.Assessment;
import com.neu.smartLesson.model.CourseClass;
import java.util.ArrayList;
import java.util.Collections;
// Removed duplicate Collectors import if it existed, keeping java.util.stream.Collectors

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper optionMapper;

    @Autowired
    private QuestionKnowledgePointMapper qkpMapper;

    @Autowired
    private CourseService courseService;

    @Autowired
    private KnowledgeGraphService kgService;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private AssessmentMapper assessmentMapper;

    @Autowired
    private AssessmentQuestionMapper aqMapper;

    @Autowired
    private ClassMapper classMapper;

    @Override
    @Transactional
    public AssessmentResponseDto generatePaper(GeneratePaperRequestDto dto, User teacher) {
        // 1. Check Class Ownership
        CourseClass clazz = classMapper.findClassById(dto.getClassId())
            .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        courseService.checkCourseOwnership(clazz.getCourseId(), teacher.getUserId());
        
        // 2. Get all questions for course
        List<Question> allQuestions = questionMapper.findQuestionsByCourseId(dto.getCourseId());

        // 3. Filter by KPs (if provided)
        if (!CollectionUtils.isEmpty(dto.getKpIds())) {
             allQuestions = allQuestions.stream().filter(q -> {
                 List<Integer> qKps = qkpMapper.findKPsByQuestionId(q.getQuestionId());
                 return dto.getKpIds().stream().anyMatch(qKps::contains);
             }).collect(Collectors.toList());
        }
        
        // 4. Select Questions by Type
        List<Question> singleChoices = allQuestions.stream().filter(q -> q.getQuestionType() == QuestionType.single_choice).collect(Collectors.toList());
        List<Question> multiChoices = allQuestions.stream().filter(q -> q.getQuestionType() == QuestionType.multi_choice).collect(Collectors.toList());
        List<Question> subjectives = allQuestions.stream().filter(q -> q.getQuestionType() == QuestionType.short_answer || q.getQuestionType() == QuestionType.report).collect(Collectors.toList());

        Collections.shuffle(singleChoices);
        Collections.shuffle(multiChoices);
        Collections.shuffle(subjectives);

        List<Integer> selectedIds = new ArrayList<>();
        if (dto.getSingleChoiceCount() != null)
            selectedIds.addAll(singleChoices.stream().limit(dto.getSingleChoiceCount()).map(Question::getQuestionId).collect(Collectors.toList()));
        if (dto.getMultiChoiceCount() != null)
            selectedIds.addAll(multiChoices.stream().limit(dto.getMultiChoiceCount()).map(Question::getQuestionId).collect(Collectors.toList()));
        if (dto.getSubjectiveCount() != null)
            selectedIds.addAll(subjectives.stream().limit(dto.getSubjectiveCount()).map(Question::getQuestionId).collect(Collectors.toList()));
        
        if(selectedIds.isEmpty()) {
             throw new ResourceNotFoundException("Not enough questions found matching criteria");
        }

        // 6. Create Assessment (Draft)
        Assessment assessment = Assessment.builder()
                .classId(dto.getClassId())
                .title(dto.getTitle())
                .assessmentType("exam") 
                .status("draft")
                .createdById(teacher.getUserId())
                .build();
        assessmentMapper.insertAssessment(assessment);

        // 7. Add Questions
        aqMapper.insertBatchQuestions(assessment.getAssessmentId(), selectedIds);

        // 8. Return DTO
         return AssessmentResponseDto.builder()
                .assessmentId(assessment.getAssessmentId())
                .classId(dto.getClassId())
                .title(assessment.getTitle())
                .status(assessment.getStatus())
                .questionIds(selectedIds)
                .build();
    }

    @Override
    @Transactional
    public QuestionFullDto createQuestion(QuestionFullDto dto, Integer courseId, User teacher) {
        // 鉴权：检查课程所有权
        courseService.checkCourseOwnership(courseId, teacher.getUserId());

        // 鉴权：检查所有 KpId 是否有效且属于本课程
        kgService.checkKnowledgePointsExist(dto.getKnowledgePointIds(), courseId);

        // 插入 Question (题干)
        Question question = Question.builder()
                .courseId(courseId)
                .prompt(dto.getPrompt())
                .questionType(QuestionType.valueOf(dto.getQuestionType()))
                .difficulty(Difficulty.valueOf(dto.getDifficulty()))
                .supportsAiGrading(dto.getSupportsAiGrading())
                .creatorId(teacher.getUserId())
                .build();
        questionMapper.insertQuestion(question); // questionId 已回填

        Integer newQuestionId = question.getQuestionId();

        // 插入 Options (选项)
        if (!CollectionUtils.isEmpty(dto.getOptions())) {
            List<QuestionOption> options = dto.getOptions().stream()
                    .map(optDto -> QuestionOption.builder()
                            .questionId(newQuestionId)
                            .optionText(optDto.getOptionText())
                            .isCorrect(optDto.getIsCorrect())
                            .build())
                    .collect(Collectors.toList());
            optionMapper.insertBatchOptions(options);
        }

        // 插入 KnowledgePoint 关联
        qkpMapper.insertBatchKPs(newQuestionId, dto.getKnowledgePointIds());

        // 返回完整的 DTO (包括新生成的 ID)
        dto.setQuestionId(newQuestionId);
        return dto;
    }

    @Override
    public List<QuestionSimpleDto> getQuestionsForCourse(Integer courseId, User teacher) {
        // 鉴权
        courseService.checkCourseOwnership(courseId, teacher.getUserId());

        // 查询 (不含 N+1)
        List<Question> questions = questionMapper.findQuestionsByCourseId(courseId);

        // 转换为 Simple DTO
        return questions.stream()
                .map(q -> QuestionSimpleDto.builder()
                        .questionId(q.getQuestionId())
                        .prompt(q.getPrompt())
                        .questionType(q.getQuestionType())
                        .difficulty(q.getDifficulty())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public QuestionFullDto getQuestionById(Integer questionId, User currentUser) {
        // 鉴权 (教师或学生)
        Question question = checkQuestionReadAccess(questionId, currentUser);

        // 组装：获取选项
        List<QuestionOptionDto> options = optionMapper.findOptionsByQuestionId(questionId).stream()
                .map(opt -> {
                    QuestionOptionDto dto = new QuestionOptionDto();
                    dto.setOptionId(opt.getOptionId());
                    dto.setOptionText(opt.getOptionText());
                    dto.setIsCorrect(opt.getIsCorrect());
                    return dto;
                }).collect(Collectors.toList());

        // 组装：获取 KpId
        List<Integer> kpIds = qkpMapper.findKPsByQuestionId(questionId);

        // 组装：返回 Full DTO
        QuestionFullDto fullDto = new QuestionFullDto();
        fullDto.setQuestionId(question.getQuestionId());
        fullDto.setPrompt(question.getPrompt());
        fullDto.setQuestionType(question.getQuestionType().name());
        fullDto.setDifficulty(question.getDifficulty().name());
        fullDto.setSupportsAiGrading(question.getSupportsAiGrading());
        fullDto.setOptions(options);
        fullDto.setKnowledgePointIds(kpIds);

        return fullDto;
    }

    @Override
    @Transactional
    public QuestionFullDto updateQuestion(Integer questionId, QuestionFullDto dto, User teacher) {
        // 鉴权：检查教师是否拥有此题目 (通过课程)
        Question question = checkQuestionOwnership(questionId, teacher.getUserId());

        // 鉴权：检查 KpId 是否有效且属于本课程
        kgService.checkKnowledgePointsExist(dto.getKnowledgePointIds(), question.getCourseId());

        // 更新 Question (题干)
        question.setPrompt(dto.getPrompt());
        question.setQuestionType(QuestionType.valueOf(dto.getQuestionType()));
        question.setDifficulty(Difficulty.valueOf(dto.getDifficulty()));
        question.setSupportsAiGrading(dto.getSupportsAiGrading());
        questionMapper.updateQuestion(question);

        // 更新 Options (采用“先删后插”策略)
        optionMapper.deleteOptionsByQuestionId(questionId);
        if (!CollectionUtils.isEmpty(dto.getOptions())) {
            List<QuestionOption> options = dto.getOptions().stream()
                    .map(optDto -> QuestionOption.builder()
                            .questionId(questionId)
                            .optionText(optDto.getOptionText())
                            .isCorrect(optDto.getIsCorrect())
                            .build())
                    .collect(Collectors.toList());
            optionMapper.insertBatchOptions(options);
        }

        // 更新 KnowledgePoint 关联 (采用“先删后插”策略)
        qkpMapper.deleteKPsByQuestionId(questionId);
        qkpMapper.insertBatchKPs(questionId, dto.getKnowledgePointIds());

        // 返回更新后的 DTO
        dto.setQuestionId(questionId);
        return dto;
    }

    @Override
    @Transactional
    public void deleteQuestion(Integer questionId, User teacher) {
        // 鉴权：检查教师是否拥有此题目
        checkQuestionOwnership(questionId, teacher.getUserId());

        // 删除 (必须按此顺序，否则违反外键约束)

        // 删除 Options
        optionMapper.deleteOptionsByQuestionId(questionId);
        // 删除 KP 关联
        qkpMapper.deleteKPsByQuestionId(questionId);
        // 删除 Question (题干)
        questionMapper.deleteQuestionById(questionId);
    }


    /**
     * 辅助：检查教师是否拥有该题目 (用于 Update/Delete)
     */
    private Question checkQuestionOwnership(Integer questionId, Integer teacherId) {
        Question question = questionMapper.findQuestionById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目 (ID: " + questionId + ") 未找到。"));

        // 检查该题目所属的课程是否属于该教师
        courseService.checkCourseOwnership(question.getCourseId(), teacherId);
        return question;
    }

    /**
     * 辅助：检查用户是否有权读取该题目 (用于 Get-Detail)
     */
    private Question checkQuestionReadAccess(Integer questionId, User currentUser) {
        Question question = questionMapper.findQuestionById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目 (ID: " + questionId + ") 未找到。"));

        if (currentUser.getRole() == Role.TEACHER) {
            // 教师：必须是课程的创建者
            courseService.checkCourseOwnership(question.getCourseId(), currentUser.getUserId());
        } else {
            // 学生：必须已注册该课程
            Integer count = enrollmentMapper.checkEnrollment(currentUser.getUserId(), question.getCourseId());
            if (count == 0) {
                throw new UnauthorizedException("您未注册此课程，无权查看此题目。");
            }
        }
        return question;
    }
}
