package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    private Integer resourceId;
    private Integer courseId;
    private String title;

    // e.g., 'video', 'pdf', 'ppt'
    private String resourceType;

    // 存储在服务器上的相对路径, e.g., "/2025/11/intro.mp4"
    private String filePath;

    private Integer uploadedById;
    private Timestamp createdAt;
}