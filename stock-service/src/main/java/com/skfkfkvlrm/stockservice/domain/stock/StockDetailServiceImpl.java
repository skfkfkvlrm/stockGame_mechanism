package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailService;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockDetailServiceImpl implements StockDetailService {
    private final StockDetailRepository stockDetailRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final com.skfkfkvlrm.stockservice.domain.admin.MarketSettingsRepository marketSettingsRepository;

    @Override
    public List<StockDetailResponse> getAllStocks() {
        List<StockDetailResponse> list = stockDetailRepository.getAllStocks();
        if (list == null) return java.util.Collections.emptyList();

        for (StockDetailResponse stock : list) {
            Integer price = stockDetailRepository.getStockPrice(stock.getStockId());
            stock.setPrice(price != null ? price : 0);

            Integer change = stockDetailRepository.getStockPriceChange(stock.getStockId());
            stock.setPriceChange(change != null ? change : 0);

            Double rate = stockDetailRepository.getChangeRate(stock.getStockId());
            stock.setChangeRate(rate != null ? rate : 0.0);

            Integer volume = stockDetailRepository.getTradeVolume(stock.getStockId());
            stock.setTradeVolume(volume != null ? volume : 0);

            LocalDateTime lastTime = stockDetailRepository.getLastTransactionTime(stock.getStockId());
            stock.setLastTransactionTime(lastTime);
        }
        return list;
    }

    @Override
    public StockDetailResponse getStockDetailInfo(int stockId) {
        StockDetailResponse stock = stockDetailRepository.getStockDetail(stockId);
        if (stock == null) {
            throw new IllegalArgumentException("존재하지 않는 종목입니다. stockId=" + stockId);
        }
        
        Integer price = stockDetailRepository.getStockPrice(stockId);
        stock.setPrice(price != null ? price : 0);

        Integer change = stockDetailRepository.getStockPriceChange(stockId);
        stock.setPriceChange(change != null ? change : 0);

        Double rate = stockDetailRepository.getChangeRate(stockId);
        stock.setChangeRate(rate != null ? rate : 0.0);

        Integer prevPrice = stockDetailRepository.getPervPrice(stockId);
        stock.setPrevPrice(prevPrice != null ? prevPrice : 0);

        Integer volume = stockDetailRepository.getTradeVolume(stockId);
        stock.setTradeVolume(volume != null ? volume : 0);

        LocalDateTime lastTime = stockDetailRepository.getLastTransactionTime(stockId);
        stock.setLastTransactionTime(lastTime);

        // VWAP 지표 계산
        stock.setVwap5(stockDetailRepository.getVwap(stockId, 5));
        stock.setVwap20(stockDetailRepository.getVwap(stockId, 20));
        stock.setVwap60(stockDetailRepository.getVwap(stockId, 60));

        // VI(정적 변동성 완화장치) 상태 계산 (기준가 대비 +-10%)
        Integer refPrice = stock.getRefPrice();
        if (refPrice != null && refPrice > 0) {
            int viHigh = (int) Math.round(refPrice * 1.10);
            int viLow = (int) Math.round(refPrice * 0.90);
            stock.setViHigh(viHigh);
            stock.setViLow(viLow);
            stock.setViActive("VI".equalsIgnoreCase(stock.getMarketStatus()));
        } else {
            stock.setViHigh(0);
            stock.setViLow(0);
            stock.setViActive(false);
        }

        return stock;
    }

    private int getIntSafe(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return 0;
        }
        return ((Number) map.get(key)).intValue();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void liquidateStudentAssets(String studentId) {
        System.out.println("[Soft Delete Asset Safe-Guard] Target Student ID: " + studentId);

        // 1. 대기 중인 모든 주문 취소 (호가 잠금 해제)
        stockDetailRepository.cancelAllWaitingOrdersByStudentId(studentId);

        // 2. 비파괴 Soft Delete 무손실 원칙에 따라 체결 완료 주식 및 포인트 몰수 제거!
        System.out.println("[Soft Delete Complete - Assets Preserved] Target Student ID: " + studentId);
    }

    @Override
    public List<Order> getLiveOrderList(int stockId, String type) {
        List<Order> orders;
        if ("매도".equals(type) || "SELL".equalsIgnoreCase(type)) {
            orders = stockDetailRepository.getTotalSellOrder(stockId);
        } else if ("매수".equals(type) || "BUY".equalsIgnoreCase(type)) {
            orders = stockDetailRepository.getTotalBuyOrder(stockId);
        } else {
            orders = stockDetailRepository.getTotalOrder(stockId);
        }
        return orders != null ? orders : java.util.Collections.emptyList();
    }

    @Override
    public List<MarketIndexResponse> getMarketIndices() {
        return stockDetailRepository.getMarketIndices();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void resetDemoStock() {
        System.out.println("[Demo Stock Reset] Clearing demo student waiting orders and confiscating matched holdings to LP...");
        stockDetailRepository.cancelAllWaitingOrdersByStudentId("demo");
        stockDetailRepository.confiscateAllStocksByStudentId("demo");
        System.out.println("[Demo Stock Reset Complete] Demo student stock reset successfully.");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void rollbackAdminDemoStock() {
        System.out.println("[Admin Demo Rollback] Restoring seed stocks 1~22 to LISTED/CONTINUOUS...");
        stockDetailRepository.restoreSeedStocks();
        System.out.println("[Admin Demo Rollback Complete] Seed stocks restored successfully.");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void resetSessionStock(boolean resetStockPrice) {
        System.out.println("[Session Stock Reset] Clearing all waiting orders and transferring student holdings to SYSTEM_LP (resetPrice=" + resetStockPrice + ")...");
        stockDetailRepository.cancelAllMarketWaitingOrders();
        stockDetailRepository.clearAllStudentHoldings();
        if (resetStockPrice) {
            stockDetailRepository.resetStockPricesToPublication();
        }
        System.out.println("[Session Stock Reset Complete] Session stock reset successfully.");
    }
}
