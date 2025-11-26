package com.neu.smartLesson.dto.analysis;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeMatrixResponseDto {
    private List<MatrixStudentDto> students;
    private List<MatrixKnowledgePointDto> knowledgePoints;
    private List<MatrixCellDto> cells;

    @Data
    public static class MatrixStudentDto {
        private Integer studentId;
        private String studentName;
    }

    @Data
    public static class MatrixKnowledgePointDto {
        private Integer kpId;
        private String kpName;
    }

    @Data
    public static class MatrixCellDto {
        private Integer kpId;
        private Integer studentId;
        private Double masteryRate;
    }
}

