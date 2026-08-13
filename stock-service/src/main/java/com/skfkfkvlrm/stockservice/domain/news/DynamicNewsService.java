package com.skfkfkvlrm.stockservice.domain.news;

import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import com.skfkfkvlrm.stockservice.domain.stock.StockListRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import com.skfkfkvlrm.stockservice.domain.stock.OrderStatus;
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
    private final StockDetailRepository stockDetailRepository;
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
            boolean isPositive = true;
            if (generatedNews == null || generatedNews.trim().isEmpty()) {
                int templateIdx = random.nextInt(REAL_NEWS_TEMPLATES.length);
                String template = REAL_NEWS_TEMPLATES[templateIdx];
                generatedNews = String.format("[%s] " + template, timestamp, targetStock.getName());
                isPositive = templateIdx != 1; // [시황] 변동성 확대를 제외하면 기본적으로 긍정 템플릿
            } else {
                generatedNews = String.format("[%s] [속보] %s", timestamp, generatedNews);
                // 뉴스 어조 판단
                isPositive = isPositiveNews(generatedNews);
            }

            newsRepository.insertNews(generatedNews);
            log.info("[DynamicNews] 등록된 종목({}) 기반 뉴스 생성 완료: {}", targetStock.getName(), generatedNews);

            // 3. 뉴스가 주가에 미치는 변동 연동 (발행잔량이 0인 완판 종목에만 적용)
            applyNewsPriceFluctuation(targetStock, isPositive);

        } catch (Exception e) {
            log.error("[DynamicNews] 뉴스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private boolean isPositiveNews(String newsText) {
        String[] negativeKeywords = {"하락", "부진", "감소", "우려", "악재", "손실", "급락", "악화", "둔화"};
        for (String kw : negativeKeywords) {
            if (newsText.contains(kw)) {
                return false;
            }
        }
        return true;
    }

    private void applyNewsPriceFluctuation(Stock stock, boolean isPositive) {
        try {
            // 발행잔량이 남아있는 경우(publication_balance > 0) 주가 변동 미적용
            if (stock.getPublicationBalance() > 0) {
                log.info("[DynamicNews] 종목({})은 발행잔량({}주)이 남아있어 뉴스 주가 변동을 적용하지 않습니다.",
                        stock.getName(), stock.getPublicationBalance());
                return;
            }

            int currentPrice = stockDetailRepository.getStockPrice(stock.getStockId());
            if (currentPrice <= 0) {
                currentPrice = stock.getPublicationPrice() > 0 ? stock.getPublicationPrice() : 1000;
            }

            // 0.1% ~ 3.0% 범위 내의 무작위 변동률 계산
            double changePercent = 0.1 + (2.9 * random.nextDouble()); // 0.1% ~ 3.0%
            if (!isPositive) {
                changePercent = -changePercent;
            }

            int newPrice = (int) Math.round(currentPrice * (1.0 + (changePercent / 100.0)));
            if (newPrice < 1) newPrice = 1; // 최소 주가 1원

            if (newPrice != currentPrice) {
                // 뉴스 변동에 따른 시스템 자동 주문 체결 기록 생성
                Order buyOrder = Order.builder()
                        .content(OrderStatus.BUY)
                        .price(newPrice)
                        .amount(1)
                        .state(OrderStatus.MATCHED)
                        .studentId("SYSTEM_NEWS")
                        .stockId(stock.getStockId())
                        .build();

                Order sellOrder = Order.builder()
                        .content(OrderStatus.SELL)
                        .price(newPrice)
                        .amount(1)
                        .state(OrderStatus.MATCHED)
                        .studentId("SYSTEM_NEWS")
                        .stockId(stock.getStockId())
                        .build();

                int systemBuyOrderId = stockDetailRepository.insertOrder(buyOrder);
                int systemSellOrderId = stockDetailRepository.insertOrder(sellOrder);
                stockDetailRepository.setMatchedOrder(systemBuyOrderId, systemSellOrderId, 1, newPrice);

                log.info("[DynamicNews] 뉴스 영향 주가 변동 완료 - 종목: {}, 어조: {}, 기존가: {}원 -> 변동가: {}원 (변동률: {}%.2f%%)",
                        stock.getName(), isPositive ? "긍정(+)" : "부정(-)", currentPrice, newPrice, changePercent);
            }
        } catch (Exception e) {
            log.error("[DynamicNews] 뉴스 연동 주가 변동 처리 실패: {}", e.getMessage(), e);
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
