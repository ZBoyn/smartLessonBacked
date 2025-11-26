package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.Answer;
import com.neu.smartLesson.model.AnswerOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.neu.smartLesson.dto.AnswerFullDetailsDto;

@Mapper
public interface AnswerMapper {
    // ... existing methods ...
    
    AnswerFullDetailsDto findFullDetailsById(@Param("answerId") Integer answerId);

    // 插入或更新答案主体
    void upsertAnswer(Answer answer);

    // 查找某次提交中特定题目的答案
    Optional<Answer> findBySubmissionIdAndQuestionId(
            @Param("submissionId") Integer submissionId,
            @Param("questionId") Integer questionId
    );

    // 批量插入选择题选项
    void insertBatchAnswerOptions(@Param("options") List<AnswerOption> options);

    // 清除旧选项 (用于重新提交)
    void deleteAnswerOptionsByAnswerId(@Param("answerId") Integer answerId);

    /**
     * 仅更新分数和评语
     */
    void updateAnswerScore(Answer answer);

    /**
     * 计算某次提交的总分
     */
    BigDecimal calculateTotalScore(@Param("submissionId") Integer submissionId);

    /**
     * 根据答案ID找提交ID
     */
    Integer findSubmissionIdByAnswerId(@Param("answerId") Integer answerId);
}