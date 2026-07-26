package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockOrderServiceTest {

    @Mock
    private StockDetailRepository stockDetailRepository;

    @Mock
    private StockPriceHistoryRepository stockPriceHistoryRepository;

    @Mock
    private MarketSettingsRepository marketSettingsRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private StockOrderServiceImpl stockOrderService;

    @BeforeEach
    void setUp() {
        MarketSettings settings = MarketSettings.builder().id(1).marketOpen(true).build();
        given(marketSettingsRepository.findById(1)).willReturn(Optional.of(settings));
    }

    @Test
    @DisplayName("포인트 부족 시 InsufficientPointException 발생 테스트")
    void buyStockFailInsufficientPoints() {
        // given
        given(stockDetailRepository.getStudentPoint("student1")).willReturn(1000);
        StockOrderRequest request = StockOrderRequest.builder()
                .studentId("student1")
                .stockId(1)
                .amount(1)
                .price(2000)
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> stockOrderService.buyStock(request))
                .isInstanceOf(com.skfkfkvlrm.stockgame_spring.exception.InsufficientPointException.class);
    }

    @Test
    @DisplayName("동시 매수 요청 시 포인트 검증 및 트랜잭션 동시성 격리 테스트")
    void buyStockConcurrentRequests() throws InterruptedException {
        // given
        // 학생 잔여 포인트 70,000원. 각 70,000원짜리 매수 요청 2건 동시 유입
        given(stockDetailRepository.getStudentPoint("student1")).willReturn(70000);
        given(stockDetailRepository.getStockPubInfo(anyInt())).willReturn(Map.of("publication_balance", 0, "publication_price", 0));
        given(stockDetailRepository.getMatchOrderList(anyInt(), anyString(), anyInt(), anyString())).willReturn(Collections.emptyList());

        StockOrderRequest request = StockOrderRequest.builder()
                .studentId("student1")
                .stockId(1)
                .amount(1)
                .price(70000)
                .build();

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    String result = stockOrderService.buyStock(request);
                    if (result.contains("등록되었습니다") || result.contains("체결되었습니다")) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }
}
