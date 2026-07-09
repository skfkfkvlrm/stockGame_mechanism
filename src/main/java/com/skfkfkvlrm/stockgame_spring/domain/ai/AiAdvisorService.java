package com.skfkfkvlrm.stockgame_spring.domain.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockInfoResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailService;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockgame_spring.domain.news.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAdvisorService {

    private final AiOllamaService aiOllamaService;
    private final StockDetailService stockDetailService;
    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper;

    public String getAdvisorMessage(int stockId) {
        // 1. 해당 주식 정보 조회
        StockDetailResponse stock = stockDetailService.getStockDetailInfo(stockId);
        if (stock == null) {
            return "해당 주식 정보를 찾을 수 없네 허허.";
        }

        // 2. 최근 뉴스 찌라시 3개 조회
        List<String> allNews = newsRepository.getNewsList();
        String recentNews = "";
        if (allNews != null && !allNews.isEmpty()) {
            int limit = Math.min(3, allNews.size());
            recentNews = String.join(" | ", allNews.subList(0, limit));
        } else {
            recentNews = "현재 특별한 뉴스는 없습니다.";
        }

        // 3. 프롬프트 구성
        String prompt = "당신은 초등학생 모의투자 게임의 친절한 투자 멘토 '버핏 할아버지'입니다. " +
                "지금 한 학생이 [" + stock.getStockName() + ", 현재가: " + stock.getNowPrice() + "원] 주식을 살펴보고 있습니다. " +
                "최근 시장 찌라시 뉴스: [" + recentNews + "]. " +
                "위 상황과 찌라시를 종합해서, 학생에게 이 주식을 지금 살지 말지(혹은 신중하라고 할지)에 대한 " +
                "위트 있고 재미있는 투자 조언을 할아버지 말투로 딱 1~2문장으로만 해주세요. " +
                "부가적인 설명 없이 오직 대사만 출력하세요.";

        // 4. Ollama 모델 호출
        String response = aiOllamaService.generateResponse(prompt).trim();
        
        // 따옴표 제거 필터링
        if (response.startsWith("\"") && response.endsWith("\"") && response.length() > 2) {
            response = response.substring(1, response.length() - 1);
        }

        log.info("[AI Advisor] 생성된 버핏 봇 조언: {}", response);
        return response;
    }
}
