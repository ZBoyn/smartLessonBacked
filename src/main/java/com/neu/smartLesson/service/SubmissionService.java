package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.SubmissionResponseDto;
import com.neu.smartLesson.dto.SubmitAnswersRequestDto;
import com.neu.smartLesson.dto.ResourceResponseDto;
import com.neu.smartLesson.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {

    /**
     * 1. 提交/保存普通答案 (文本/选择)
     * 会自动创建 'in_progress' 状态的 Submission
     */
    void saveAnswers(Integer assessmentId, SubmitAnswersRequestDto dto, User student);

    /**
     * 2. 上传报告题文件 (Task 3 重点)
     */
    ResourceResponseDto uploadAnswerFile(Integer assessmentId, Integer questionId, MultipartFile file, User student);

    /**
     * 3. 最终确认提交 (结束考试/作业)
     */
    SubmissionResponseDto finalizeSubmission(Integer assessmentId, User student);
}