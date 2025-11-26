package com.neu.smartLesson.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class VideoAnalytics {
    private Integer analyticsId;
    private Integer resourceId;
    private Integer studentId;
    private Double completionRate;
    private String heatmapData;
    private Timestamp lastWatchedAt;
}

