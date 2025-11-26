package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.analysis.KnowledgeMatrixResponseDto;
import com.neu.smartLesson.dto.analysis.KnowledgeRadarResponseDto;
import com.neu.smartLesson.dto.analysis.LearningBehaviorPointDto;
import com.neu.smartLesson.dto.analysis.VideoLearningStatsDto;
import com.neu.smartLesson.model.User;

import java.util.List;

public interface LearningAnalyticsService {

    KnowledgeRadarResponseDto getKnowledgeRadar(Integer classId, User teacher);

    KnowledgeMatrixResponseDto getKnowledgeMatrix(Integer classId, User teacher);

    VideoLearningStatsDto getVideoLearningStats(Integer classId, User teacher);

    List<LearningBehaviorPointDto> getLearningBehaviorCorrelation(Integer classId, User teacher);
}

