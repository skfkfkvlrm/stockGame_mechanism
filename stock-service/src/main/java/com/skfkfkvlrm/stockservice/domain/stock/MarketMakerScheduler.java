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
    private static final int VWAP_LIMIT = 20;
    private static final double MIN_MARGIN = 0.01;
    private static final double MAX_MARGIN = 0.04;
    private static final int LP_ORDER_AMOUNT = 10;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void executeMarketMaking() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null || !settings.calculateIsMarketOpen() || !"OPEN".equalsIgnoreCase(settings.calculateStatusCode())) {
            return;
        }

        List<Stock> stocks = stockListRepository.getAllStocks();
        for (Stock stock : stocks) {
            try {
                processLiquidityProviding(stock.getStockId());
            } catch (Exception e) {
                log.error("[MarketMaker] LP bot exception for stock {}", stock.getStockId(), e);
            }
        }
    }

    private void processLiquidityProviding(int stockId) {
        Map<String, Object> stockInfo = stockDetailRepository.getStockInfoForUpdate(stockId);
        if (stockInfo == null || !"CONTINUOUS".equalsIgnoreCase((String) stockInfo.get("marketStatus"))) {
            return;
        }

        LocalDateTime lastTxTime = stockDetailRepository.getLastTransactionTime(stockId);
        if (lastTxTime == null) {
            lastTxTime = LocalDateTime.now().minusMinutes(60);
        }

        long inactivityMinutes = ChronoUnit.MINUTES.between(lastTxTime, LocalDateTime.now());
        if (inactivityMinutes < 20) {
            return;
        }
        
        if (inactivityMinutes < 40 && Math.random() > 0.05) {
            return;
        }

        Integer highestBuy = stockDetailRepository.getHighestBuyPrice(stockId);
        Integer lowestSell = stockDetailRepository.getLowestSellPrice(stockId);
        
        int currentPrice = stockDetailRepository.getStockPrice(stockId);
        
        boolean needsLiquidity = false;
        if (highestBuy == null || lowestSell == null) {
            needsLiquidity = true;
        } else {
            double spread = (double) (lowestSell - highestBuy) / lowestSell;
            if (spread >= 0.02) {
                needsLiquidity = true;
            }
        }

        if (!needsLiquidity) {
            return;
        }

        Integer vwap = stockDetailRepository.getVwap(stockId, VWAP_LIMIT);
        if (vwap == null || vwap == 0) {
            vwap = currentPrice;
        }

        log.info("🤖 [MarketMaker] Distributing liquidity for stock {} (inactivity {}m)", stockId, inactivityMinutes);

        cancelExistingLpOrders(stockId);

        distributeLpOrders(stockId, OrderStatus.BUY, vwap, LP_ORDER_AMOUNT);
        distributeLpOrders(stockId, OrderStatus.SELL, vwap, LP_ORDER_AMOUNT);

        messagingTemplate.convertAndSend("/topic/orders/" + stockId, "ORDER_UPDATED");
    }

    private void distributeLpOrders(int stockId, OrderStatus content, int vwap, int totalAmount) {
        int remainingAmount = totalAmount;
        int splits = 3 + (int) (Math.random() * 3); 

        for (int i = 0; i < splits; i++) {
            if (remainingAmount <= 0) break;

            int amount = (i == splits - 1) ? remainingAmount : Math.max(1, (int) (Math.random() * (remainingAmount / 2 + 1)));
            if (amount == 0) amount = 1;

            double randomMargin = MIN_MARGIN + (Math.random() * (MAX_MARGIN - MIN_MARGIN));
            int price = (content == OrderStatus.BUY)
                    ? (int) (vwap * (1.0 - randomMargin))
                    : (int) (vwap * (1.0 + randomMargin));
            
            price = Math.max(1, price);

            insertLpOrder(stockId, content, price, amount);
            remainingAmount -= amount;
        }
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