package com.neu.smartLesson.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建课程时 (US-T01) 的请求 DTO
 */
@Data
public class CreateCourseRequestDto {

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 255, message = "课程名称过长")
    private String courseName;

    private String description;
}