package com.neu.smartLesson.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KpMasteryDto {
    private Integer kpId;
    private String kpName;

    // 该知识点在本次测评中的总分值 (分母)
    private Double totalPointsPossible;

    // 班级学生在该知识点上的总得分 (分子)
    private Double totalPointsObtained;

    // 掌握度 (0.0 - 1.0) = obtained / possible
    private Double masteryRate;
}