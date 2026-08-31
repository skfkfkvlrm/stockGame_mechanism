package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaticViProcessor {

    private final StockDetailRepository stockDetailRepository;
    private final ClosingAuctionService closingAuctionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public void triggerStaticVi(int stockId) {
        log.info("🚨 [Static VI] 종목 {} 정적 VI 발동! 2분 대기 시작.", stockId);
        
        messagingTemplate.convertAndSend("/topic/orders/" + stockId, "STATIC_VI_TRIGGERED");

        scheduler.schedule(() -> {
            try {
                releaseStaticVi(stockId);
            } catch (Exception e) {
                log.error("[Static VI] 종목 {} 단일가 체결 중 오류 발생", stockId, e);
            }
        }, 2, TimeUnit.MINUTES);
    }

    private void releaseStaticVi(int stockId) {
        log.info("🔓 [Static VI] 종목 {} 정적 VI 해제 및 단일가 일괄 체결 시작.", stockId);

        closingAuctionService.matchSinglePriceAuctionForStock(stockId);
        int currentPrice = stockDetailRepository.getStockPrice(stockId);

        updateViStatus(stockId, currentPrice);

        messagingTemplate.convertAndSend("/topic/orders/" + stockId, "STATIC_VI_RELEASED");
        log.info("✅ [Static VI] 종목 {} 정규장 복귀 완료 (새로운 기준가: {})", stockId, currentPrice);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateViStatus(int stockId, int currentPrice) {
        stockDetailRepository.updateMarketStatus(stockId, "CONTINUOUS");
        stockDetailRepository.updateRefPrice(stockId, currentPrice);
    }
}
