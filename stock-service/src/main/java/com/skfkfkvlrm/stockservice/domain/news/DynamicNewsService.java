package com.skfkfkvlrm.stockservice.domain.news;

import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import com.skfkfkvlrm.stockservice.domain.stock.StockListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class DynamicNewsService {

    private final NewsRepository newsRepository;
    private final StockListRepository stockListRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final String MODEL_NAME = "qwen2.5-coder:7b";
    private final Random random = new Random();

    private final String[] REAL_NEWS_TEMPLATES = {
        "[속보] %s 관련 신제품 및 신규 마케팅 발표로 시장 관심 대폭 집중",
        "[시황] %s 전일 대비 거래량 급증, 주가 변동성 확대 양상",
        "[투자이슈] %s 주요 서비스 이용률 증가에 따른 호조세 기록",
        "[증시뉴스] %s 관련 유저 긍정 평가 확산으로 시장 기대감 고조",
        "[시장동향] %s 매수세 유입 속 관련 테마주 동반 상승 기류"
    };

    /**
     * 5분마다 등록된 종목(예: 새콤달콤, PC방, SM) 기반 실제 뉴스/Ollama AI 뉴스 자동 생성
     */
    @Scheduled(fixedRate = 300000, initialDelay = 1000)
    public void generateStockNews() {
        try {
            List<Stock> stocks = stockListRepository.getAllStocks();
            if (stocks == null || stocks.isEmpty()) {
                log.warn("[DynamicNews] 등록된 주식 종목이 없어 뉴스를 생성하지 않습니다.");
                return;
            }

            // 'LISTED' 상장 종목만 필터링
            List<Stock> listedStocks = stocks.stream()
                    .filter(s -> s.getStatus() == null || "LISTED".equalsIgnoreCase(s.getStatus()))
                    .collect(Collectors.toList());

            if (listedStocks.isEmpty()) {
                listedStocks = stocks;
            }

            Stock targetStock = listedStocks.get(random.nextInt(listedStocks.size()));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // 1. Ollama LLM 호출 시도 (등록된 종목명 기반)
            String generatedNews = callOllamaForStockNews(targetStock, listedStocks);

            // 2. Ollama 응답 실패 시 실제 뉴스 템플릿 기반 Fallback 생성
            if (generatedNews == null || generatedNews.trim().isEmpty()) {
                String template = REAL_NEWS_TEMPLATES[random.nextInt(REAL_NEWS_TEMPLATES.length)];
                generatedNews = String.format("[%s] " + template, timestamp, targetStock.getName());
            } else {
                generatedNews = String.format("[%s] [속보] %s", timestamp, generatedNews);
            }

            newsRepository.insertNews(generatedNews);
            log.info("[DynamicNews] 등록된 종목({}) 기반 뉴스 생성 완료: {}", targetStock.getName(), generatedNews);

        } catch (Exception e) {
            log.error("[DynamicNews] 뉴스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private String callOllamaForStockNews(Stock targetStock, List<Stock> allListedStocks) {
        try {
            String stockListStr = allListedStocks.stream()
                    .map(s -> s.getName() + "(현재가: " + s.getPrevPrice() + "원)")
                    .collect(Collectors.joining(", "));

            String prompt = "현재 등록된 주식 종목 목록: [" + stockListStr + "]. " +
                    "이 중 '" + targetStock.getName() + "' 종목에 관한 실제 증시 기사이거나 재미있는 속보 뉴스 1문장을 한국어로 작성해주세요. " +
                    "다른 종목 이름(삼성전자, SK하이닉스 등)은 절대 언급하지 말고 오직 '" + targetStock.getName() + "' 종목만 다루세요. " +
                    "부연 설명이나 인사말 없이 오직 뉴스 1문장만 출력하세요.";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String resText = (String) response.getBody().get("response");
                if (resText != null) {
                    resText = resText.trim();
                    if (resText.startsWith("\"") && resText.endsWith("\"")) {
                        resText = resText.substring(1, resText.length() - 1);
                    }
                    return resText;
                }
            }
        } catch (Exception e) {
            log.warn("[DynamicNews] Ollama 호출 실패. Fallback 템플릿 사용. 사유: {}", e.getMessage());
        }
        return null;
    }
}
