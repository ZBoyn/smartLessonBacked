package com.neu.smartLesson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

/**
 * DTO 用于在 Service 层和 Controller 层之间传递文件流及其元数据
 */
@Data
@AllArgsConstructor
public class StreamResourceDto {
    private Resource resourceFile; // 文件本身
    private String filename;          // 用于 Content-Disposition
}