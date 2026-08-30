package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockPriceResponse;
import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockListRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockPriceService;
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

            // 2. 해당 종목의 발행 정보 (잔여 수량, 발행가)
            Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(stockId);
            int pubAmount = getIntOrDefault(pubInfo, "pubAmount");
            int pubPrice = getIntOrDefault(pubInfo, "pubPrice");

            // 3. 현재가 조회 (최근 체결가 또는 발행가)
            int currentPrice = stockDetailRepository.getStockPrice(stockId);
            if (currentPrice == 0) currentPrice = pubPrice;

            // 4. 전일 종가 조회
            int prevPrice = stockDetailRepository.getPervPrice(stockId);

            // 5. 전일 대비 변동액 및 등락률 계산
            int priceChange = currentPrice - prevPrice;
            double changeRate = 0.0;
            if (prevPrice != 0) {
                changeRate = (double) priceChange / prevPrice * 100;
                changeRate = Math.round(changeRate * 100.0) / 100.0;
            }

            // 6. StockPriceResponse 생성 및 추가
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
