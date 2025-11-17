package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.KnowledgePointDto;
import com.neu.smartLesson.model.KnowledgePoint;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgePointDtoMapper {

    public KnowledgePoint toModel(KnowledgePointDto dto, Integer courseId) {
        return KnowledgePoint.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .courseId(courseId)
                .build();
    }

    public KnowledgePointDto toDto(KnowledgePoint model) {
        return KnowledgePointDto.builder()
                .kpId(model.getKpId())
                .name(model.getName())
                .description(model.getDescription())
                .courseId(model.getCourseId())
                .build();
    }

    public List<KnowledgePointDto> toDtoList(List<KnowledgePoint> models) {
        return models.stream().map(this::toDto).collect(Collectors.toList());
    }
}