package com.neu.smartLesson.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 查看课程时 (US-T02) 返回的响应 DTO
 */
@Data
@Builder
public class CourseResponseDto {

    private Integer courseId;
    private String courseName;
    private String description;
    private Integer creatorId;
}