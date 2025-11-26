package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.QuestionFullDto;
import com.neu.smartLesson.dto.QuestionSimpleDto;
import com.neu.smartLesson.model.User;
import java.util.List;

public interface QuestionService {
    // (Create)
    QuestionFullDto createQuestion(QuestionFullDto dto, Integer courseId, User teacher);

    // (Read-List)
    List<QuestionSimpleDto> getQuestionsForCourse(Integer courseId, User teacher);

    // (Read-Detail)
    QuestionFullDto getQuestionById(Integer questionId, User currentUser);

    // (Update)
    QuestionFullDto updateQuestion(Integer questionId, QuestionFullDto dto, User teacher);

    // (Delete)
    void deleteQuestion(Integer questionId, User teacher);
    
    // (Generate Paper)
    com.neu.smartLesson.dto.AssessmentResponseDto generatePaper(com.neu.smartLesson.dto.GeneratePaperRequestDto dto, User teacher);
}