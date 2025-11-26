package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.analysis.KnowledgeMatrixResponseDto;
import com.neu.smartLesson.dto.analysis.KnowledgeRadarResponseDto;
import com.neu.smartLesson.dto.analysis.LearningBehaviorPointDto;
import com.neu.smartLesson.dto.analysis.RadarIndicatorDto;
import com.neu.smartLesson.dto.analysis.VideoLearningStatsDto;
import com.neu.smartLesson.dto.analysis.KpMasteryDto;
import com.neu.smartLesson.dto.analysis.StudentKpMasteryDto;
import com.neu.smartLesson.mapper.AnalyticsMapper;
import com.neu.smartLesson.mapper.VideoAnalyticsMapper;
import com.neu.smartLesson.mapper.ResourceMapper;
import com.neu.smartLesson.mapper.EnrollmentMapper;
import com.neu.smartLesson.mapper.KnowledgePointMapper;
import com.neu.smartLesson.mapper.ClassMapper;
import com.neu.smartLesson.model.CourseClass;
import com.neu.smartLesson.model.Resource;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.model.VideoAnalytics;
import com.neu.smartLesson.service.CourseService;
import com.neu.smartLesson.service.LearningAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningAnalyticsServiceImpl implements LearningAnalyticsService {

    @Autowired
    private AnalyticsMapper analyticsMapper;
    @Autowired
    private VideoAnalyticsMapper videoAnalyticsMapper;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;
    @Autowired
    private ClassMapper classMapper;
    @Autowired
    private CourseService courseService;

    private CourseClass check(Integer classId, User teacher) {
        CourseClass clazz = classMapper.findClassById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        courseService.checkCourseOwnership(clazz.getCourseId(), teacher.getUserId());
        return clazz;
    }

    @Override
    public KnowledgeRadarResponseDto getKnowledgeRadar(Integer classId, User teacher) {
        CourseClass clazz = check(classId, teacher);
        List<KpMasteryDto> kpMastery = analyticsMapper.analyzeClassOverallMastery(classId);
        List<StudentKpMasteryDto> studentKp = analyticsMapper.analyzeStudentKpMastery(classId);
        int totalStudents = Optional.ofNullable(enrollmentMapper.countByClassId(classId)).orElse(0);

        Map<Integer, List<StudentKpMasteryDto>> byKp = studentKp.stream()
                .collect(Collectors.groupingBy(StudentKpMasteryDto::getKpId));

        List<RadarIndicatorDto> indicators = new ArrayList<>();
        List<Double> classAvg = new ArrayList<>();
        List<Double> topStudent = new ArrayList<>();
        List<Double> passRate = new ArrayList<>();

        if (kpMastery.isEmpty()) {
            List<com.neu.smartLesson.model.KnowledgePoint> kps = knowledgePointMapper
                    .findKnowledgePointsByCourseId(clazz.getCourseId());
            for (com.neu.smartLesson.model.KnowledgePoint kp : kps) {
                indicators.add(new RadarIndicatorDto(kp.getName(), 1.0));
                classAvg.add(0.0);
                List<StudentKpMasteryDto> arr = byKp.getOrDefault(kp.getKpId(), Collections.emptyList());
                double top = arr.stream().map(StudentKpMasteryDto::getMasteryRate).max(Double::compare).orElse(0.0);
                topStudent.add(top);
                long passed = arr.stream().filter(x -> Optional.ofNullable(x.getMasteryRate()).orElse(0.0) >= 0.6)
                        .count();
                passRate.add(totalStudents == 0 ? 0.0 : (double) passed / (double) totalStudents);
            }
        } else {
            for (KpMasteryDto km : kpMastery) {
                indicators.add(new RadarIndicatorDto(km.getKpName(), 1.0));
                classAvg.add(Optional.ofNullable(km.getMasteryRate()).orElse(0.0));
                List<StudentKpMasteryDto> arr = byKp.getOrDefault(km.getKpId(), Collections.emptyList());
                double top = arr.stream().map(StudentKpMasteryDto::getMasteryRate).max(Double::compare).orElse(0.0);
                topStudent.add(top);
                long passed = arr.stream().filter(x -> Optional.ofNullable(x.getMasteryRate()).orElse(0.0) >= 0.6)
                        .count();
                passRate.add(totalStudents == 0 ? 0.0 : (double) passed / (double) totalStudents);
            }
        }

        KnowledgeRadarResponseDto dto = new KnowledgeRadarResponseDto();
        dto.setIndicators(indicators);
        dto.setClassAvg(classAvg);
        dto.setTopStudent(topStudent);
        dto.setPassRate(passRate);
        return dto;
    }

    @Override
    public KnowledgeMatrixResponseDto getKnowledgeMatrix(Integer classId, User teacher) {
        CourseClass clazz = check(classId, teacher);
        List<User> students = enrollmentMapper.findStudentsByClassId(classId);
        List<com.neu.smartLesson.model.KnowledgePoint> kps = knowledgePointMapper
                .findKnowledgePointsByCourseId(clazz.getCourseId());
        List<StudentKpMasteryDto> mastery = analyticsMapper.analyzeStudentKpMastery(classId);

        Map<String, Double> keyToRate = new HashMap<>();
        for (StudentKpMasteryDto m : mastery) {
            String key = m.getKpId() + ":" + m.getStudentId();
            keyToRate.put(key, Optional.ofNullable(m.getMasteryRate()).orElse(0.0));
        }

        KnowledgeMatrixResponseDto dto = new KnowledgeMatrixResponseDto();
        List<KnowledgeMatrixResponseDto.MatrixStudentDto> sDtos = new ArrayList<>();
        for (User u : students) {
            KnowledgeMatrixResponseDto.MatrixStudentDto s = new KnowledgeMatrixResponseDto.MatrixStudentDto();
            s.setStudentId(u.getUserId());
            s.setStudentName(u.getFullName());
            sDtos.add(s);
        }
        dto.setStudents(sDtos);

        List<KnowledgeMatrixResponseDto.MatrixKnowledgePointDto> kpDtos = new ArrayList<>();
        for (com.neu.smartLesson.model.KnowledgePoint kp : kps) {
            KnowledgeMatrixResponseDto.MatrixKnowledgePointDto k = new KnowledgeMatrixResponseDto.MatrixKnowledgePointDto();
            k.setKpId(kp.getKpId());
            k.setKpName(kp.getName());
            kpDtos.add(k);
        }
        dto.setKnowledgePoints(kpDtos);

        List<KnowledgeMatrixResponseDto.MatrixCellDto> cells = new ArrayList<>();
        for (com.neu.smartLesson.model.KnowledgePoint kp : kps) {
            for (User u : students) {
                KnowledgeMatrixResponseDto.MatrixCellDto c = new KnowledgeMatrixResponseDto.MatrixCellDto();
                c.setKpId(kp.getKpId());
                c.setStudentId(u.getUserId());
                Double rate = keyToRate.getOrDefault(kp.getKpId() + ":" + u.getUserId(), 0.0);
                c.setMasteryRate(Optional.ofNullable(rate).orElse(0.0));
                cells.add(c);
            }
        }
        dto.setCells(cells);
        return dto;
    }

    @Override
    public VideoLearningStatsDto getVideoLearningStats(Integer classId, User teacher) {
        CourseClass clazz = check(classId, teacher);
        List<Resource> videos = resourceMapper.findVideoResourcesByCourseId(clazz.getCourseId());
        List<Integer> resourceIds = videos.stream().map(Resource::getResourceId).collect(Collectors.toList());
        List<VideoAnalytics> analytics = resourceIds.isEmpty() ? Collections.emptyList()
                : videoAnalyticsMapper.findByResourceIds(resourceIds);

        Set<Integer> viewersAll = analytics.stream().map(VideoAnalytics::getStudentId).collect(Collectors.toSet());
        double overallCompletion = analytics.stream().map(VideoAnalytics::getCompletionRate).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgEngagement = overallCompletion;

        List<VideoLearningStatsDto.VideoLearningItemDto> items = new ArrayList<>();
        Map<Integer, List<VideoAnalytics>> byRes = analytics.stream()
                .collect(Collectors.groupingBy(VideoAnalytics::getResourceId));
        for (Resource r : videos) {
            List<VideoAnalytics> list = byRes.getOrDefault(r.getResourceId(), Collections.emptyList());
            double avgComp = list.stream().map(VideoAnalytics::getCompletionRate).filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue).average().orElse(0.0);
            Set<Integer> viewers = list.stream().map(VideoAnalytics::getStudentId).collect(Collectors.toSet());
            VideoLearningStatsDto.VideoLearningItemDto item = new VideoLearningStatsDto.VideoLearningItemDto();
            item.setResourceId(r.getResourceId());
            item.setTitle(r.getTitle());
            item.setAvgCompletion(avgComp);
            item.setEngagementScore(avgComp);
            item.setViewerCount(viewers.size());
            item.setHeatmap(new ArrayList<>());
            items.add(item);
        }

        VideoLearningStatsDto dto = new VideoLearningStatsDto();
        dto.setOverallCompletion(overallCompletion);
        dto.setAvgEngagement(avgEngagement);
        dto.setTotalViewers(viewersAll.size());
        dto.setVideos(items);
        return dto;
    }

    @Override
    public List<LearningBehaviorPointDto> getLearningBehaviorCorrelation(Integer classId, User teacher) {
        check(classId, teacher);
        List<Map<String, Object>> engagement = videoAnalyticsMapper.summarizeEngagementByClass(classId);
        Map<Integer, Double> engagementByStudent = new HashMap<>();
        for (Map<String, Object> row : engagement) {
            Object sid = row.get("studentId");
            Object es = row.get("engagementScore");
            if (sid != null && es != null) {
                engagementByStudent.put(Integer.parseInt(sid.toString()), Double.parseDouble(es.toString()));
            }
        }
        List<com.neu.smartLesson.dto.analysis.StudentAnalysisDto> students = analyticsMapper
                .analyzeStudentOverall(classId);
        List<LearningBehaviorPointDto> points = new ArrayList<>();
        for (com.neu.smartLesson.dto.analysis.StudentAnalysisDto s : students) {
            Double e = engagementByStudent.getOrDefault(s.getStudentId(), 0.0);
            Double finalScore = s.getAvgScore() == null ? 0.0 : s.getAvgScore().doubleValue();
            Double subRate = Optional.ofNullable(s.getSubmissionRate()).orElse(0.0);
            points.add(new LearningBehaviorPointDto(s.getStudentId(), s.getName(), e, finalScore, subRate));
        }
        return points;
    }
}
