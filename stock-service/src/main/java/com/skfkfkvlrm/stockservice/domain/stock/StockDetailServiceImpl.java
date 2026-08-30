package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skfkfkvlrm.stockservice.domain.stock.StockListRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockDetailServiceImpl implements StockDetailService {
    private final StockDetailRepository stockDetailRepository;
    private final StockListRepository stockListRepository;

    // 1. 주식 기본 정보 조회
    @Override
    public StockDetailResponse getStockDetailInfo(int stockId) {
        Map<String, Object> stockInfo = stockDetailRepository.getStockInfo(stockId);
        if (stockInfo == null) {
            throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.STOCK_NOT_FOUND);
        }

        // 2. 주식 발행 정보 조회
        Map<String, Object> stockPubInfo = stockDetailRepository.getStockPubInfo(stockId);

        int pubPrice = getIntOrDefault(stockPubInfo, "pubPrice");
        if (pubPrice == 0) pubPrice = getIntOrDefault(stockPubInfo, "publication_price");
        if (pubPrice == 0) pubPrice = getIntOrDefault(stockPubInfo, "publicationPrice");

        int pubAmount = getIntOrDefault(stockPubInfo, "pubAmount");
        if (pubAmount == 0) pubAmount = getIntOrDefault(stockPubInfo, "publication_balance");
        if (pubAmount == 0) pubAmount = getIntOrDefault(stockPubInfo, "publicationBalance");
        // 3. 현재 시장 가격 조회
        int nowPrice = stockDetailRepository.getStockPrice(stockId);
        nowPrice = nowPrice == 0 ? pubPrice : nowPrice;
        // 4. 이전 날 가격 및 유저 간 거래량(발행 잔량 제외) 조회
        int prevPrice = stockDetailRepository.getPervPrice(stockId);
        int tradeVolume = stockDetailRepository.getTradeVolume(stockId);

        String status = (String) stockInfo.get("status");
        if (status == null) status = "LISTED";

        // 5. response 빌드 후 반환
        return new StockDetailResponse(
                stockId,
                (String) stockInfo.get("name"),
                (String) stockInfo.get("content"),
                nowPrice,
                prevPrice,
                pubPrice,
                pubAmount,
                tradeVolume,
                status
        );
    }

    @Override
    public List<StockDetailResponse> getAllStocks() {
        List<Stock> stocks = stockListRepository.getAllStocks();
        if (stocks == null) return Collections.emptyList();
        return stocks.stream().map(s -> {
            int nowPrice = stockDetailRepository.getStockPrice(s.getStockId());
            nowPrice = nowPrice == 0 ? s.getPublicationPrice() : nowPrice;
            int tradeVol = stockDetailRepository.getTradeVolume(s.getStockId());
            return new StockDetailResponse(
                    s.getStockId(),
                    s.getName(),
                    s.getContent(),
                    nowPrice,
                    s.getPrevPrice(),
                    s.getPublicationPrice(),
                    s.getPublicationBalance(),
                    tradeVol,
                    s.getStatus() != null ? s.getStatus() : "LISTED"
            );
        }).collect(Collectors.toList());
    }

    private int getIntOrDefault(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return 0;
        }
        return ((Number) map.get(key)).intValue();
    }

    /**
     * 주식 거래 호가창 실시간 주문 목록 조회
     * 
     * @param stockId 주식 종목 ID
     * @param type    호가 주문 구분 ("매도" / "SELL" 또는 "매수" / "BUY")
     * @return 호가창에 표시할 대기 주문 목록
     */
    @Override
    public List<Order> getLiveOrderList(int stockId, String type) {
        if ("SELL".equalsIgnoreCase(type) || "매도".equalsIgnoreCase(type)) {
            List<Order> sellOrders = stockDetailRepository.getTotalSellOrder(stockId);
            return sellOrders != null ? sellOrders : Collections.emptyList();
        } else {
            List<Order> buyOrders = stockDetailRepository.getTotalBuyOrder(stockId);
            return buyOrders != null ? buyOrders : Collections.emptyList();
        }
    }

    @Override
    public List<Order> getwaitingOrderList(int stockId, String studentId) {
        List<Order> myOrders = stockDetailRepository.getTotalMyOrder(stockId, studentId);
        return myOrders != null ? myOrders : Collections.emptyList();
    }

    @Override
    public List<MarketIndexResponse> getMarketIndices() {
        List<StockDetailResponse> stocks = getAllStocks();
        if (stocks == null || stocks.isEmpty()) {
            return List.of(
                MarketIndexResponse.builder().name("KOSPI").value(2750.24).change(12.45).changeRate(0.45).build(),
                MarketIndexResponse.builder().name("KOSDAQ").value(845.12).change(-3.20).changeRate(-0.38).build()
            );
        }

        double totalNow = 0;
        double totalPrev = 0;
        for (StockDetailResponse s : stocks) {
            totalNow += s.getNowPrice();
            totalPrev += (s.getPrevPrice() > 0 ? s.getPrevPrice() : s.getNowPrice());
        }

        double kospiBase = 2750.0;
        double kosdaqBase = 845.0;

        double overallChangeRate = totalPrev > 0 ? ((totalNow - totalPrev) / totalPrev) : 0;
        double kospiPrev = 2737.79;
        double kosdaqPrev = 848.32;

        double kospiValue = Math.round((kospiBase * (1 + overallChangeRate)) * 100.0) / 100.0;
        double kospiChange = Math.round((kospiValue - kospiPrev) * 100.0) / 100.0;
        double kospiRate = Math.round(((kospiValue - kospiPrev) / kospiPrev * 100.0) * 100.0) / 100.0;

        double kosdaqValue = Math.round((kosdaqBase * (1 + (overallChangeRate * 0.8))) * 100.0) / 100.0;
        double kosdaqChange = Math.round((kosdaqValue - kosdaqPrev) * 100.0) / 100.0;
        double kosdaqRate = Math.round(((kosdaqValue - kosdaqPrev) / kosdaqPrev * 100.0) * 100.0) / 100.0;

        return List.of(
            MarketIndexResponse.builder()
                .name("KOSPI")
                .value(kospiValue)
                .change(kospiChange)
                .changeRate(kospiRate)
                .prevClose(kospiPrev)
                .openPrice(Math.round((kospiPrev + 2.3) * 100.0) / 100.0)
                .highPrice(Math.round((Math.max(kospiValue, kospiPrev) + 8.5) * 100.0) / 100.0)
                .lowPrice(Math.round((Math.min(kospiValue, kospiPrev) - 6.2) * 100.0) / 100.0)
                .high52w(2890.50)
                .low52w(2273.97)
                .volume(458290000L)
                .tradingValue(9820300000000L)
                .chartHistory(List.of(2720.5, 2735.2, 2741.0, 2738.4, 2745.8, 2737.79, kospiValue))
                .build(),
            MarketIndexResponse.builder()
                .name("KOSDAQ")
                .value(kosdaqValue)
                .change(kosdaqChange)
                .changeRate(kosdaqRate)
                .prevClose(kosdaqPrev)
                .openPrice(Math.round((kosdaqPrev - 1.1) * 100.0) / 100.0)
                .highPrice(Math.round((Math.max(kosdaqValue, kosdaqPrev) + 4.2) * 100.0) / 100.0)
                .lowPrice(Math.round((Math.min(kosdaqValue, kosdaqPrev) - 5.8) * 100.0) / 100.0)
                .high52w(920.10)
                .low52w(735.40)
                .volume(892400000L)
                .tradingValue(7450200000000L)
                .chartHistory(List.of(855.2, 852.0, 849.5, 847.2, 849.8, 848.32, kosdaqValue))
                .build()
        );
    }
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delistStock(int stockId, int compensationPrice, String reason) {
        System.out.println("[Delisting Start] Target Stock ID: " + stockId + ", CompPrice: " + compensationPrice + ", Reason: " + reason);

        // 1. Status -> DELISTED
        stockListRepository.updateStockStatusToDelisted(stockId);

        // 2. Cancel waiting orders and refund BUY
        List<Order> waitingOrders = stockDetailRepository.getWaitingOrdersByStockId(stockId);
        for(Order o : waitingOrders) {
            if ("BUY".equalsIgnoreCase(o.getContent().name()) || "매수".equals(o.getContent().name())) {
                stockDetailRepository.setStudentPointUp(o.getPrice() * o.getAmount(), o.getStudentId());
                System.out.println(" - Refund: " + o.getStudentId() + ", Amount: " + (o.getPrice() * o.getAmount()));
            }
        }
        stockDetailRepository.cancelWaitingOrdersByStockId(stockId);

        // 3. Clear holdings
        List<java.util.Map<String, Object>> holdings = stockDetailRepository.getHoldingsByStockId(stockId);
        for(java.util.Map<String, Object> h : holdings) {
            String studentId = (String) h.get("studentId");
            if (studentId == null) {
                studentId = (String) h.get("student_id");
            }
            if (studentId == null || "SYSTEM_LP".equals(studentId)) continue;
            
            int amount = 0;
            if (h.get("amount") instanceof Number) {
                amount = ((Number) h.get("amount")).intValue();
            }

            if (amount > 0) {
                if (compensationPrice > 0) {
                    stockDetailRepository.setStudentPointUp(amount * compensationPrice, studentId);
                    System.out.println(" - Holding Compensation: " + studentId + ", Points: " + (amount * compensationPrice));
                }
                
                Order forceSellOrder = Order.builder()
                        .content(OrderStatus.SELL)
                        .state(OrderStatus.MATCHED)
                        .price(compensationPrice)
                        .amount(amount)
                        .studentId(studentId)
                        .stockId(stockId)
                        .build();
                stockDetailRepository.insertOrder(forceSellOrder);
            }
        }
        
        System.out.println("[Delisting End] Target Stock ID: " + stockId);
    }
}
