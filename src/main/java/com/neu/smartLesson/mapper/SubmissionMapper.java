package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.StudentSubmissionSummaryDto;
import com.neu.smartLesson.model.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SubmissionMapper {
    void insertSubmission(Submission submission);
    void updateSubmission(Submission submission);

    /**
     * 查找学生对某次测评的提交记录
     */
    Optional<Submission> findByAssessmentIdAndStudentId(
            @Param("assessmentId") Integer assessmentId,
            @Param("studentId") Integer studentId
    );

    Optional<Submission> findById(@Param("submissionId") Integer submissionId);

    /**
     * 查询某次测评的所有学生提交情况 (包含学生姓名)
     */
    List<StudentSubmissionSummaryDto> findSummaryByAssessmentId(@Param("assessmentId") Integer assessmentId);

    /**
     * 仅更新总分和状态
     */
    void updateScoreAndStatus(Submission submission);
}