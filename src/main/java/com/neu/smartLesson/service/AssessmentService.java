package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.AssessmentResponseDto;
import com.neu.smartLesson.dto.CreateAssessmentRequestDto;
import com.neu.smartLesson.dto.PublishAssessmentRequestDto;
import com.neu.smartLesson.dto.StudentAssessmentDetailDto;
import com.neu.smartLesson.model.User;

public interface AssessmentService {

    /**
     * (US15, 16) 教师为班级创建测评 (草稿)
     */
    AssessmentResponseDto createAssessment(CreateAssessmentRequestDto dto, Integer classId, User teacher);

    /**
     * (US18) 教师发布测评
     */
    AssessmentResponseDto publishAssessment(PublishAssessmentRequestDto dto, Integer assessmentId, User teacher);

    /**
     * 获取班级的所有测评
     */
    java.util.List<AssessmentResponseDto> getAssessmentsByClass(Integer classId, User teacher);

    /**
     * 学生获取测评详情
     */
    StudentAssessmentDetailDto getAssessmentDetailForStudent(Integer assessmentId, User student);
}
