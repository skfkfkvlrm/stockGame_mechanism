package com.skfkfkvlrm.stockservice.domain.news;

import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import com.skfkfkvlrm.stockservice.domain.stock.StockListRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockPriceHistoryRepository;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import com.skfkfkvlrm.stockservice.domain.stock.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final StockDetailRepository stockDetailRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ollama.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    @Value("${ollama.model:qwen2.5-coder:7b}")
    private String modelName;

    private final Random random = new Random();

    private final String[] POSITIVE_NEWS_TEMPLATES = {
        "[호재] %s 관련 신제품 및 신규 마케팅 발표로 시장 관심 대폭 집중",
        "[시황] %s 전일 대비 매수세 유입 급증, 주가 강세 기조 지속",
        "[투자이슈] %s 주요 서비스 이용률 폭증에 따른 분기 실적 호조 기대",
        "[증시뉴스] %s 유저 만족도 및 브랜드 인지도 상승으로 호평 잇따라",
        "[시장동향] %s 신규 사업 호조세에 증권가 목표 주가 상향 잇따라"
    };

    private final String[] NEGATIVE_NEWS_TEMPLATES = {
        "[악재] %s 원가 상승 및 일시적 수요 둔화로 단기 수익성 악화 우려",
        "[시황] %s 차익 실현 매물 대거 출회되며 주가 하방 압력 지속",
        "[투자경고] %s 동종 업계 경쟁 심화에 따른 시장 점유율 일시 하락세",
        "[증시뉴스] %s 주요 서비스 일시 장애 및 이용자 불만 접수",
        "[시장동향] %s 매도세 우위 속 단기 가격 조정 국면 진입"
    };

    /**
     * 5분마다 등록된 종목(예: 새콤달콤, PC방, SM) 기반 실제 뉴스/Ollama AI 뉴스 자동 생성 (긍정:부정 = 6:4 비율)
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

            // 긍정:부정 = 6:4 비율 적용 (60% 확률 긍정, 40% 확률 부정)
            boolean isPositive = random.nextDouble() < 0.6;
            String sentimentLabel = isPositive ? "호재" : "악재";

            // 1. Ollama LLM 호출 시도 (감성 방향 주입)
            String generatedNews = callOllamaForStockNews(targetStock, listedStocks, isPositive);

            if (generatedNews == null || generatedNews.trim().isEmpty()) {
                String[] templatePool = isPositive ? POSITIVE_NEWS_TEMPLATES : NEGATIVE_NEWS_TEMPLATES;
                int templateIdx = random.nextInt(templatePool.length);
                String template = templatePool[templateIdx];
                generatedNews = String.format("[%s] " + template, timestamp, targetStock.getName());
            } else {
                generatedNews = String.format("[%s] [%s] %s", timestamp, sentimentLabel, generatedNews);
            }

            newsRepository.insertNews(generatedNews);
            log.info("[DynamicNews] 종목({}) 기반 [{}] 뉴스 생성 완료: {}", targetStock.getName(), sentimentLabel, generatedNews);

            // 뉴스 브로드캐스트
            messagingTemplate.convertAndSend("/topic/news", generatedNews);

        } catch (Exception e) {
            log.error("[DynamicNews] 뉴스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private String callOllamaForStockNews(Stock targetStock, List<Stock> allListedStocks, boolean isPositive) {
        try {
            String stockListStr = allListedStocks.stream()
                    .map(s -> s.getName() + "(현재가: " + s.getPrevPrice() + "원)")
                    .collect(Collectors.joining(", "));

            String sentimentRequirement = isPositive
                    ? "반드시 매출 증가, 신제품 성공, 이용자 급증 등 **긍정적인 호재 뉴스**"
                    : "반드시 매출 부진, 경쟁 심화, 서비스 일시 장애, 비용 증가 등 **부정적인 악재 뉴스**";

            String prompt = "현재 등록된 주식 종목 목록: [" + stockListStr + "]. " +
                    "이 중 '" + targetStock.getName() + "' 종목에 관하여 " + sentimentRequirement + " 1문장을 한국어로 작성해주세요. " +
                    "다른 종목 이름(삼성전자, SK하이닉스 등)은 절대 언급하지 말고 오직 '" + targetStock.getName() + "' 종목만 다루세요. " +
                    "부연 설명이나 큰따옴표, 인사말 없이 오직 완성된 뉴스 기사 1문장만 출력하세요.";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl, request, Map.class);

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
