package com.neu.smartLesson.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.smartLesson.dto.AiGradingResultDto;
import com.neu.smartLesson.service.AiGradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiGradingServiceImpl implements AiGradingService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Override
    public AiGradingResultDto gradeSingleAnswer(String studentId, String questionContent, String studentAnswer,
                                                String referenceAnswer, Integer maxScore) {
        String url = aiServiceUrl + "/api/grading/single";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("student_id", studentId);
        body.add("question_content", questionContent);
        body.add("student_answer", studentAnswer);
        if (referenceAnswer != null) {
            body.add("reference_answer", referenceAnswer);
        }
        body.add("max_score", maxScore);
        // Default params
        body.add("enable_knowledge_analysis", true);
        body.add("enable_plagiarism_detection", false);
        body.add("use_dynamic_rubric", true);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                
                if (root.get("success").asBoolean()) {
                    JsonNode result = root.get("result");
                    
                    Double score = result.get("total_score").asDouble();
                    String summary = result.get("summary").asText();
                    
                    // Handle suggestions (List -> String)
                    JsonNode suggestionsNode = result.get("suggestions");
                    List<String> suggestionsList = new ArrayList<>();
                    if (suggestionsNode.isArray()) {
                        for (JsonNode node : suggestionsNode) {
                            suggestionsList.add(node.asText());
                        }
                    }
                    String suggestions = String.join("; ", suggestionsList);
                    
                    // Handle dimensions (JSON -> String)
                    String dimensions = result.get("dimensions").toString();

                    return AiGradingResultDto.builder()
                            .totalScore(score)
                            .summary(summary)
                            .suggestions(suggestions)
                            .dimensions(dimensions)
                            .build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error or return null/empty result
        }
        return null;
    }
}

