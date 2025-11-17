package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.RelationResponseDto;
import com.neu.smartLesson.model.KnowledgeRelation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgeRelationDtoMapper {

    public RelationResponseDto toDto(KnowledgeRelation model) {
        return RelationResponseDto.builder()
                .relationId(model.getRelationId())
                .sourceKpId(model.getSourceKpId())
                .targetKpId(model.getTargetKpId())
                .relationType(model.getRelationType())
                .build();
    }

    public List<RelationResponseDto> toDtoList(List<KnowledgeRelation> models) {
        return models.stream().map(this::toDto).collect(Collectors.toList());
    }
}