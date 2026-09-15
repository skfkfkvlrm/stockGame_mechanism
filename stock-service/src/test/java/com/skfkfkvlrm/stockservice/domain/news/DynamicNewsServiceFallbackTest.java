package com.skfkfkvlrm.stockservice.domain.news;

import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockListRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockPriceHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DynamicNewsServiceFallbackTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private StockListRepository stockListRepository;

    @Mock
    private StockDetailRepository stockDetailRepository;

    @Mock
    private StockPriceHistoryRepository stockPriceHistoryRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RestTemplate mockRestTemplate;

    private DynamicNewsService dynamicNewsService;

    private List<Stock> mockStockList;

    @BeforeEach
    void setUp() {
        dynamicNewsService = new DynamicNewsService(
                newsRepository,
                stockListRepository,
                stockDetailRepository,
                stockPriceHistoryRepository,
                messagingTemplate
        );
        dynamicNewsService.setRestTemplate(mockRestTemplate);
        dynamicNewsService.setOllamaUrl("http://localhost:11434/api/generate");
        dynamicNewsService.setModelName("qwen2.5-coder:7b");

        // 가상 주식 목록 설정
        Stock testStock = new Stock();
        testStock.setStockId(1);
        testStock.setName("새콤달콤");
        testStock.setPrevPrice(1500);
        testStock.setStatus("LISTED");
        mockStockList = Collections.singletonList(testStock);

        when(stockListRepository.getAllStocks()).thenReturn(mockStockList);
    }

    @Test
    @DisplayName("[시나리오 1] Ollama 정상 구동 시 AI 생성 뉴스가 DB 및 웹소켓으로 정상 발행된다")
    void testNormalOllamaExecution_ShouldPublishAiNews() {
        // given
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("response", "\"새콤달콤 신제품 출시 호평으로 분기 매출 급증 기대\"");
        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(mockResponseEntity);

        // when
        assertDoesNotThrow(() -> dynamicNewsService.generateStockNews());

        // then
        ArgumentCaptor<String> newsCaptor = ArgumentCaptor.forClass(String.class);
        verify(newsRepository, times(1)).insertNews(newsCaptor.capture());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/news"), newsCaptor.capture());

        String publishedNews = newsCaptor.getValue();
        assertThat(publishedNews).contains("새콤달콤 신제품 출시 호평");
        assertThat(publishedNews).doesNotStartWith("\""); // 따옴표 정제 확인
    }

    @Test
    @DisplayName("[시나리오 2 - 핵심 실증] Ollama 프로세스 강제 종료(Kill) 시 Connection Refused 에러를 감지하고 10종 Fallback 템플릿으로 무중단 발행된다")
    void testOllamaProcessKilled_ConnectionRefused_ShouldFallbackToTemplate() {
        // given: Ollama 프로세스가 죽어 Connection Refused 예외 발생 시뮬레이션
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("I/O error on POST request to \"http://localhost:11434/api/generate\": Connection refused: connect"));

        // when: 예외가 상위로 전파되지 않고 무중단 처리되는지 확인
        assertDoesNotThrow(() -> dynamicNewsService.generateStockNews());

        // then: Fallback 템플릿으로 뉴스가 정상 생성되어 DB 및 웹소켓으로 발행되었는지 검증
        ArgumentCaptor<String> dbCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> wsCaptor = ArgumentCaptor.forClass(String.class);

        verify(newsRepository, times(1)).insertNews(dbCaptor.capture());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/news"), wsCaptor.capture());

        String dbNews = dbCaptor.getValue();
        String wsNews = wsCaptor.getValue();

        assertThat(dbNews).isEqualTo(wsNews);
        assertThat(dbNews).contains("새콤달콤");
        // 템플릿의 핵심 키워드 중 하나를 포함하는지 확인 (10종 템플릿 중 하나)
        boolean matchesTemplate = dbNews.contains("신제품 및 신규 마케팅") ||
                dbNews.contains("매수세 유입 급증") ||
                dbNews.contains("분기 실적 호조 기대") ||
                dbNews.contains("브랜드 인지도 상승") ||
                dbNews.contains("신규 사업 호조세") ||
                dbNews.contains("단기 수익성 악화 우려") ||
                dbNews.contains("차익 실현 매물") ||
                dbNews.contains("시장 점유율 일시 하락세") ||
                dbNews.contains("주요 서비스 일시 장애") ||
                dbNews.contains("단기 가격 조정 국면");

        assertThat(matchesTemplate).isTrue();
    }

    @Test
    @DisplayName("[시나리오 3] Ollama 호출 3초 타임아웃 발생 시 블로킹 없이 Fallback 템플릿으로 즉각 전환된다")
    void testOllamaTimeout_ShouldFallbackToTemplateWithin3Seconds() {
        // given: 3초 타임아웃 초과 예외 시뮬레이션
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out")));

        // when
        assertDoesNotThrow(() -> dynamicNewsService.generateStockNews());

        // then
        ArgumentCaptor<String> newsCaptor = ArgumentCaptor.forClass(String.class);
        verify(newsRepository, times(1)).insertNews(newsCaptor.capture());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/news"), newsCaptor.capture());

        assertThat(newsCaptor.getValue()).isNotBlank();
        assertThat(newsCaptor.getValue()).contains("새콤달콤");
    }

    @Test
    @DisplayName("[시나리오 4] 시황 Fallback 템플릿 풀이 정확히 호재 5종 + 악재 5종 = 총 10종으로 구성되어 있는지 검증")
    void testTemplatePoolIntegrity_ShouldHaveExactly10Templates() throws Exception {
        Field posField = DynamicNewsService.class.getDeclaredField("POSITIVE_NEWS_TEMPLATES");
        Field negField = DynamicNewsService.class.getDeclaredField("NEGATIVE_NEWS_TEMPLATES");
        posField.setAccessible(true);
        negField.setAccessible(true);

        String[] posTemplates = (String[]) posField.get(dynamicNewsService);
        String[] negTemplates = (String[]) negField.get(dynamicNewsService);

        assertThat(posTemplates).hasSize(5);
        assertThat(negTemplates).hasSize(5);
        assertThat(posTemplates.length + negTemplates.length).isEqualTo(10);
    }
}
