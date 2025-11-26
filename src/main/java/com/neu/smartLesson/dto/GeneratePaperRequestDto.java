package com.neu.smartLesson.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeneratePaperRequestDto {
    private String title;
    private Integer courseId;
    private Integer classId; // Added classId
    private List<Integer> kpIds;
    private Integer difficulty; // 1-5
    private Integer singleChoiceCount;
    private Integer multiChoiceCount;
    private Integer subjectiveCount;
}
