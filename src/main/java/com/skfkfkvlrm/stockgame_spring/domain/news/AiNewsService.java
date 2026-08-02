package com.skfkfkvlrm.stockgame_spring.domain.news;

import com.skfkfkvlrm.stockgame_spring.domain.ai.AiOllamaService;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiNewsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiNewsService.class);

    private final AiOllamaService aiOllamaService;
    private final StockDetailService stockDetailService;
    private final NewsRepository newsRepository;

    public void generateDynamicNews() {
        // 1. 현재 주식 정보 수집
        List<StockDetailResponse> stocks = stockDetailService.getAllStocks();
        String stockStatus = stocks.stream()
                .map(s -> s.getStockName() + ": " + s.getNowPrice() + "원")
                .collect(Collectors.joining(", "));

        // 2. 프롬프트 구성
        String prompt = "현재 주식 시장 상황입니다: [" + stockStatus + "]. " +
                "이 게임은 초등학생 대상 모의투자 게임입니다. 위 주식 중 1개를 골라서, " +
                "가격이 변동할만한 아주 재미있고 약간은 과장된 찌라시(가짜 속보) 1문장만 한국어로 작성해주세요. " +
                "이유나 부연 설명 없이 오직 생성된 뉴스 1문장만 출력하세요.";

        // 3. Ollama 모델에 요청
        String generatedNews = aiOllamaService.generateResponse(prompt).trim();

        // 불필요한 따옴표 제거나 포맷팅 등 정제
        if (generatedNews.startsWith("\"") && generatedNews.endsWith("\"")) {
            generatedNews = generatedNews.substring(1, generatedNews.length() - 1);
        }

        // 4. DB에 저장
        newsRepository.insertNews(generatedNews);
        log.info("[AI News] 생성된 뉴스 DB 저장 완료: {}", generatedNews);
    }
}
