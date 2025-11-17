package com.neu.smartLesson.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用于创建(Request)和查看(Response)知识点的 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointDto {

    private Integer kpId;

    @NotBlank(message = "知识点名称不能为空")
    private String name;

    private String description;

    // 我们在响应时会填充这个字段
    private Integer courseId;
}