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
            throw new IllegalArgumentException("존재하지 않는 주식입니다. 주식 번호: " + stockId);
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

        // 5. response 빌드 후 반환
        return StockDetailResponse.builder()
                .stockId(stockId)
                .stockName((String) stockInfo.get("name"))
                .content((String) stockInfo.get("content"))
                .nowPrice(nowPrice)
                .prevPrice(prevPrice)
                .pubPrice(pubPrice)
                .pubAmount(pubAmount)
                .build();
    }

    private int getIntOrDefault(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return 0;
        }
        return ((Number) map.get(key)).intValue();
    }

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
    public List<StockDetailResponse> getAllStocks() {
        return stockListRepository.getStockNameList().stream()
                .map(stock -> getStockDetailInfo(stock.getStockId()))
                .collect(Collectors.toList());
    }
}
