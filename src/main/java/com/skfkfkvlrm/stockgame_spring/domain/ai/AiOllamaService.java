package com.skfkfkvlrm.stockgame_spring.domain.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiOllamaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiOllamaService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final String MODEL_NAME = "gemma4";

    /**
     * 프롬프트를 전송하고 Ollama API로부터 응답을 받아옵니다.
     */
    public String generateResponse(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("[Ollama] Requesting AI generation with model: {}", MODEL_NAME);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_URL, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("response");
            }
        } catch (Exception e) {
            log.error("[Ollama] AI 생성 요청 실패: {}", e.getMessage());
        }
        return "AI 뉴스 생성 실패 (모델 통신 오류)";
    }
}
