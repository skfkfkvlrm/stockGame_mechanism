package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockservice.domain.admin.MarketSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 장 마감 동시호가(단일가 매매, Closing Call Auction) 배치 및 단일가 일괄 체결 서비스
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ClosingAuctionService {

    private final StockDetailRepository stockDetailRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final MarketSettingsRepository marketSettingsRepository;
    private final StockListRepository stockListRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 매 1분마다 장 마감 시간(closeTime, 기본 15:30) 체크 후 일괄 체결 실행
     */
    @Scheduled(cron = "0 * * * * *") // 매 분 0초마다 실행
    public void checkAndExecuteClosingAuction() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null || !"AUTO".equalsIgnoreCase(settings.getMode())) {
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime close = LocalTime.parse(settings.getCloseTime() != null ? settings.getCloseTime() : "15:30", formatter);
            LocalTime now = LocalTime.now();

            // 마감 시간 정각(분 단위 일치)에 단 1회 실행
            if (now.getHour() == close.getHour() && now.getMinute() == close.getMinute()) {
                log.info("🔔 [장 마감 동시호가] 정규장 마감 시간({}) 도달 - 전 종목 단일가 일괄 체결 시작", close);
                executeClosingAuctionForAllStocks();
            }
        } catch (Exception e) {
            log.error("동시호가 마감 스케줄러 실행 오류", e);
        }
    }

    /**
     * 전 종목 대상 장 마감 동시호가 단일가 산출 및 일괄 체결 실행 (수동/자동 공통)
     */
    @Transactional
    public Map<String, Object> executeClosingAuctionForAllStocks() {
        List<Stock> stocks = stockListRepository.getAllStocks();
        Map<String, Object> resultSummary = new HashMap<>();
        int totalExecutedTrades = 0;

        for (Stock stock : stocks) {
            int stockId = stock.getStockId();
            int executedCount = matchSinglePriceAuctionForStock(stockId);
            totalExecutedTrades += executedCount;
            resultSummary.put(stock.getName(), executedCount + "건 체결 완료");
        }

        log.info("🎯 [장 마감 동시호가] 총 {}건의 단일가 매매 일괄 체결 완료", totalExecutedTrades);
        return resultSummary;
    }

    /**
     * 개별 종목별 동시호가 단일가(Single Price) 산출 및 체결
     */
    @Transactional
    public int matchSinglePriceAuctionForStock(int stockId) {
        List<Order> buyOrders = stockDetailRepository.getTotalBuyOrder(stockId);
        List<Order> sellOrders = stockDetailRepository.getTotalSellOrder(stockId);

        if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
            return 0;
        }

        // 1. 단일가(Single Price / 종가) 산출: 최대 거래량이 체결되는 최적 균형 가격 탐색
        Integer singlePrice = calculateOptimalSinglePrice(buyOrders, sellOrders);
        if (singlePrice == null) {
            return 0;
        }

        // 2. 단일가 이상 매수 호가 및 단일가 이하 매도 호가 추출
        List<Order> eligibleBuys = new ArrayList<>();
        for (Order o : buyOrders) {
            if (o.getPrice() >= singlePrice) {
                eligibleBuys.add(o);
            }
        }

        List<Order> eligibleSells = new ArrayList<>();
        for (Order o : sellOrders) {
            if (o.getPrice() <= singlePrice) {
                eligibleSells.add(o);
            }
        }

        // 매수는 높은 가격순/시간순, 매도는 낮은 가격순/시간순 정렬
        eligibleBuys.sort((a, b) -> {
            if (b.getPrice() != a.getPrice()) return Integer.compare(b.getPrice(), a.getPrice());
            return 0;
        });
        eligibleSells.sort((a, b) -> {
            if (a.getPrice() != b.getPrice()) return Integer.compare(a.getPrice(), b.getPrice());
            return 0;
        });

        int executedCount = 0;
        int buyIdx = 0;
        int sellIdx = 0;

        while (buyIdx < eligibleBuys.size() && sellIdx < eligibleSells.size()) {
            Order b = eligibleBuys.get(buyIdx);
            Order s = eligibleSells.get(sellIdx);

            if (b.getStudentId().equals(s.getStudentId())) {
                // 자전거래 방지
                sellIdx++;
                continue;
            }

            int matchAmount = Math.min(b.getAmount(), s.getAmount());
            if (matchAmount <= 0) break;

            int matchTotalPrice = singlePrice * matchAmount;

            // 매수 주문 체결 처리
            int buyOrderId;
            if (matchAmount == b.getAmount()) {
                stockDetailRepository.setOrderStateMatched(b.getOrderId());
                buyOrderId = b.getOrderId();
                buyIdx++;
            } else {
                stockDetailRepository.updateOrderAmount(b.getAmount() - matchAmount, b.getOrderId());
                b.setAmount(b.getAmount() - matchAmount);
                Order buyFilled = Order.builder()
                        .content(OrderStatus.BUY).state(OrderStatus.MATCHED)
                        .price(singlePrice).amount(matchAmount)
                        .studentId(b.getStudentId()).stockId(stockId).build();
                stockDetailRepository.insertOrder(buyFilled);
                buyOrderId = buyFilled.getOrderId();
            }

            // 매도 주문 체결 처리
            int sellOrderId;
            if (matchAmount == s.getAmount()) {
                stockDetailRepository.setOrderStateMatched(s.getOrderId());
                sellOrderId = s.getOrderId();
                sellIdx++;
            } else {
                stockDetailRepository.updateOrderAmount(s.getAmount() - matchAmount, s.getOrderId());
                s.setAmount(s.getAmount() - matchAmount);
                Order sellFilled = Order.builder()
                        .content(OrderStatus.SELL).state(OrderStatus.MATCHED)
                        .price(singlePrice).amount(matchAmount)
                        .studentId(s.getStudentId()).stockId(stockId).build();
                stockDetailRepository.insertOrder(sellFilled);
                sellOrderId = sellFilled.getOrderId();
            }

            // 거래내역 및 포인트 정산 (단일가 singlePrice로 정산)
            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, singlePrice);

            // 매도자에게 포인트 입금
            if (!"SYSTEM_LP".equals(s.getStudentId())) {
                stockDetailRepository.setStudentPointUp(matchTotalPrice, s.getStudentId());
            }

            // 일별 시세 갱신 (종가 업데이트)
            stockPriceHistoryRepository.upsertDailyPrice(stockId, LocalDate.now(), singlePrice, matchAmount);

            executedCount++;
        }

        // 호가창 및 차트 웹소켓 알림
        messagingTemplate.convertAndSend("/topic/orders/" + stockId, "ORDER_UPDATED");
        return executedCount;
    }

    /**
     * 최대 거래량을 도출하는 단일가(Single Price) 산출 알고리즘
     */
    private Integer calculateOptimalSinglePrice(List<Order> buyOrders, List<Order> sellOrders) {
        Set<Integer> candidatePrices = new TreeSet<>();
        for (Order o : buyOrders) candidatePrices.add(o.getPrice());
        for (Order o : sellOrders) candidatePrices.add(o.getPrice());

        int maxVolume = 0;
        Integer optimalPrice = null;

        for (int price : candidatePrices) {
            // 해당 가격 이상으로 살 수 있는 총 매수 수량
            int cumulativeBuy = 0;
            for (Order b : buyOrders) {
                if (b.getPrice() >= price) cumulativeBuy += b.getAmount();
            }

            // 해당 가격 이하로 팔 수 있는 총 매도 수량
            int cumulativeSell = 0;
            for (Order s : sellOrders) {
                if (s.getPrice() <= price) cumulativeSell += s.getAmount();
            }

            int executableVolume = Math.min(cumulativeBuy, cumulativeSell);
            if (executableVolume > maxVolume) {
                maxVolume = executableVolume;
                optimalPrice = price;
            }
        }

        return maxVolume > 0 ? optimalPrice : null;
    }
}
