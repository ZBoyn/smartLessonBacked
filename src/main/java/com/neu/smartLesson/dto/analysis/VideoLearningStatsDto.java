package com.neu.smartLesson.dto.analysis;

import lombok.Data;

import java.util.List;

@Data
public class VideoLearningStatsDto {
    private Double overallCompletion;
    private Double avgEngagement;
    private Integer totalViewers;
    private List<VideoLearningItemDto> videos;

    @Data
    public static class VideoLearningItemDto {
        private Integer resourceId;
        private String title;
        private Double avgCompletion;
        private Double engagementScore;
        private Integer viewerCount;
        private List<VideoHeatPointDto> heatmap;
    }

    @Data
    public static class VideoHeatPointDto {
        private Integer bucketIndex;
        private String label;
        private Double intensity;
    }
}

