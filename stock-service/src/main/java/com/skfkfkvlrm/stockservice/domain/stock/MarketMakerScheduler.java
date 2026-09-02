package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockservice.domain.admin.MarketSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MarketMakerScheduler {

    private final StockDetailRepository stockDetailRepository;
    private final StockListRepository stockListRepository;
    private final MarketSettingsRepository marketSettingsRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String LP_STUDENT_ID = "SYSTEM_LP";
    private static final int VWAP_LIMIT = 20; // 최근 20건 기준
    private static final double MARGIN_RATE = 0.05; // 5% 스프레드 마진
    private static final int LP_ORDER_AMOUNT = 10; // 호가당 10주 공급

    @Scheduled(cron = "0 * * * * *") // 매 분 0초 실행
    @Transactional
    public void executeMarketMaking() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null || !settings.calculateIsMarketOpen() || !"OPEN".equalsIgnoreCase(settings.calculateStatusCode())) {
            return; // 장이 열려있지 않거나, 동시호가 상태면 LP 로직 중단
        }

        List<Stock> stocks = stockListRepository.getAllStocks();
        for (Stock stock : stocks) {
            try {
                processLiquidityProviding(stock.getStockId());
            } catch (Exception e) {
                log.error("[MarketMaker] 종목 {} LP 봇 실행 중 예외 발생", stock.getStockId(), e);
            }
        }
    }

    private void processLiquidityProviding(int stockId) {
        // 1. 상태 검증
        Map<String, Object> stockInfo = stockDetailRepository.getStockInfoForUpdate(stockId);
        if (stockInfo == null || !"CONTINUOUS".equalsIgnoreCase((String) stockInfo.get("marketStatus"))) {
            return;
        }

        // 2. 거래 공백 기간 검증 (20~40분 랜덤 개입)
        LocalDateTime lastTxTime = stockDetailRepository.getLastTransactionTime(stockId);
        if (lastTxTime == null) {
            lastTxTime = LocalDateTime.now().minusMinutes(60); // 거래내역이 없으면 무조건 개입 대상으로 간주
        }

        long inactivityMinutes = ChronoUnit.MINUTES.between(lastTxTime, LocalDateTime.now());
        if (inactivityMinutes < 20) {
            return; // 20분 미만이면 개입 안 함
        }
        
        // 20~40분 사이일 때 매 분마다 5%의 확률로 개입 (패턴화 방지)
        if (inactivityMinutes < 40 && Math.random() > 0.05) {
            return;
        }

        // 3. 현재 오더북 스프레드 5% 초과 검사
        Integer highestBuy = stockDetailRepository.getHighestBuyPrice(stockId);
        Integer lowestSell = stockDetailRepository.getLowestSellPrice(stockId);
        
        int currentPrice = stockDetailRepository.getStockPrice(stockId);
        
        boolean needsLiquidity = false;
        if (highestBuy == null || lowestSell == null) {
            needsLiquidity = true;
        } else {
            double spread = (double) (lowestSell - highestBuy) / lowestSell;
            if (spread >= 0.05) {
                needsLiquidity = true;
            }
        }

        if (!needsLiquidity) {
            return;
        }

        // 4. VWAP 계산 및 LP 호가 제출
        Integer vwap = stockDetailRepository.getVwap(stockId, VWAP_LIMIT);
        if (vwap == null || vwap == 0) {
            vwap = currentPrice;
        }

        int lpBuyPrice = (int) (vwap * (1.0 - MARGIN_RATE));
        int lpSellPrice = (int) (vwap * (1.0 + MARGIN_RATE));
        
        // 최소 호가 단위 적용 (음수 방지)
        lpBuyPrice = Math.max(1, lpBuyPrice);

        log.info("🤖 [MarketMaker] 종목 {} 유동성 공급 (공백 {}분) - 매수: {}, 매도: {}", stockId, inactivityMinutes, lpBuyPrice, lpSellPrice);

        // 기존 LP 주문 취소 (호가 누적 방지)
        cancelExistingLpOrders(stockId);

        // 매수/매도 주문 직접 Insert (포인트, 주식 잔고 우회)
        insertLpOrder(stockId, OrderStatus.BUY, lpBuyPrice, LP_ORDER_AMOUNT);
        insertLpOrder(stockId, OrderStatus.SELL, lpSellPrice, LP_ORDER_AMOUNT);

        // 프론트엔드 호가창 갱신 브로드캐스트
        messagingTemplate.convertAndSend("/topic/orders/" + stockId, "ORDER_UPDATED");
    }

    private void cancelExistingLpOrders(int stockId) {
        List<Order> waitingOrders = stockDetailRepository.getWaitingOrdersByStockId(stockId);
        for (Order order : waitingOrders) {
            if (LP_STUDENT_ID.equals(order.getStudentId())) {
                stockDetailRepository.setOrderStateCancel(order.getOrderId());
            }
        }
    }

    private void insertLpOrder(int stockId, OrderStatus content, int price, int amount) {
        Order order = Order.builder()
                .content(content)
                .state(OrderStatus.WAITING)
                .price(price)
                .amount(amount)
                .studentId(LP_STUDENT_ID)
                .stockId(stockId)
                .build();
        stockDetailRepository.insertOrder(order);
    }
}
