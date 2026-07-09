package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.StockPriceResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockListRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockPriceServiceImpl implements StockPriceService {
    private final StockDetailRepository stockDetailRepository;
    private final StockListRepository stockListRepository;

    @Override
    public List<StockPriceResponse> getStockPriceList() {
        // 1. 전체 주식 종목 리스트 조회
        List<Stock> stockList = stockListRepository.getStockNameList();
        List<StockPriceResponse> stockPriceList = new ArrayList<>();

        for (Stock stock : stockList) {
            int stockId = stock.getStockId();
            String stockName = stock.getName();

            // 2. 해당 종목의 발행 정보(잔여 수량, 발행가)
            Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(stockId);
            int pubAmount = getIntOrDefault(pubInfo, "pubAmount");
            int pubPrice = getIntOrDefault(pubInfo, "pubPrice");

            // 3. 발행 잔량이 남아있으면 현재가를 초기 발행가로 고정
            int currentPrice = pubAmount > 0 ? pubPrice : stockDetailRepository.getStockPrice(stockId);

            // 4. 이전 가격 조회
            int prevPrice = stockDetailRepository.getPervPrice(stockId);

            // 5. 전일 대비 변동액 및 등락률
            int priceChange = currentPrice - prevPrice;
            double changeRate = 0.0;
            if (prevPrice != 0) {
                changeRate = (double) priceChange / prevPrice * 100;
                changeRate = Math.round(changeRate * 100.0) / 100.0;
            }

            // 6. StockPriceResponse에 저장
            stockPriceList.add(StockPriceResponse.builder()
                    .stockId(stockId)
                    .stockName(stockName)
                    .currentPrice(currentPrice)
                    .prevPrice(prevPrice)
                    .priceChange(priceChange)
                    .changeRate(changeRate)
                    .build());
        }
        return stockPriceList;
    }

    private int getIntOrDefault(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return 0;
        }
        return ((Number) map.get(key)).intValue();
    }
}
