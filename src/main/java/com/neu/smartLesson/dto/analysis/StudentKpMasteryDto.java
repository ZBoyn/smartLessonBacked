package com.neu.smartLesson.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentKpMasteryDto {
    private Integer kpId;
    private String kpName;
    private Integer studentId;
    private String studentName;
    /**
     * 掌握度 0~1
     */
    private Double masteryRate;
}

