package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Order;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skfkfkvlrm.stockgame_spring.domain.stock.StockListRepository;

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
            throw new com.skfkfkvlrm.stockgame_spring.exception.StockNotFoundException();
        }

        // 2. 주식 발행 정보 조회
        Map<String, Object> stockPubInfo = stockDetailRepository.getStockPubInfo(stockId);

        int pubPrice = getIntOrDefault(stockPubInfo, "pubPrice");
        int pubAmount = getIntOrDefault(stockPubInfo, "pubAmount");
        // 3. 현재 시장 가격 조회
        int nowPrice = stockDetailRepository.getStockPrice(stockId);
        nowPrice = nowPrice == 0 ? pubPrice : nowPrice;
        // 4. 이전 장 가격 조회
        int prevPrice = stockDetailRepository.getPervPrice(stockId);

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
            return new StockDetailResponse(
                    s.getStockId(),
                    s.getName(),
                    s.getContent(),
                    nowPrice,
                    s.getPrevPrice(),
                    s.getPublicationPrice(),
                    s.getPublicationBalance(),
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
     * 주식 거래 호가창 라이브 주문 목록 조회 (교차 매핑 디자인 패턴)
     * 
     * [도메인 설계 의도]:
     * - 사용자가 매수 탭(type == "매수")을 클릭했을 때: 체결 대상이 되는 상대방의 '매도 호가 목록'(getTotalSellOrder)을 반환.
     * - 사용자가 매도 탭(type == "매도")을 클릭했을 때: 체결 대상이 되는 상대방의 '매수 호가 목록'(getTotalBuyOrder)을 반환.
     * 따라서 파라미터 type과 반환하는 호가 목록의 매도/매수가 교차(Cross-Mapping)되는 것은 호가창 표준 디자인 로직입니다.
     * 
     * @param stockId 주식 종목 ID
     * @param type    현재 사용자의 거래 탭 구분 ("매수" 또는 "매도")
     * @return 호가창에 노출할 대기 주문 목록
     */
    @Override
    public List<Order> getLiveOrderList(int stockId, String type) {
        if ("매수".equalsIgnoreCase(type)) {
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

        double kospiValue = Math.round((kospiBase * (1 + overallChangeRate)) * 100.0) / 100.0;
        double kospiChange = Math.round((kospiValue - kospiBase) * 100.0) / 100.0;
        double kospiRate = Math.round((overallChangeRate * 100.0) * 100.0) / 100.0;

        double kosdaqValue = Math.round((kosdaqBase * (1 + (overallChangeRate * 0.8))) * 100.0) / 100.0;
        double kosdaqChange = Math.round((kosdaqValue - kosdaqBase) * 100.0) / 100.0;
        double kosdaqRate = Math.round(((overallChangeRate * 0.8) * 100.0) * 100.0) / 100.0;

        return List.of(
            MarketIndexResponse.builder().name("KOSPI").value(kospiValue).change(kospiChange).changeRate(kospiRate).build(),
            MarketIndexResponse.builder().name("KOSDAQ").value(kosdaqValue).change(kosdaqChange).changeRate(kosdaqRate).build()
        );
    }
}
