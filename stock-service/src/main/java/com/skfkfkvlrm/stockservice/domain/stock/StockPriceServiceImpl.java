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
        // 1. ?泥?二쇱 醫紐?由ъㅽ?議고
        List<Stock> stockList = stockListRepository.getStockNameList();
        List<StockPriceResponse> stockPriceList = new ArrayList<>();

        for (Stock stock : stockList) {
            int stockId = stock.getStockId();
            String stockName = stock.getName();

            // 2. ?대?醫紐⑹ 諛? ?蹂??????, 諛?媛)
            Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(stockId);
            int pubAmount = getIntOrDefault(pubInfo, "pubAmount");
            int pubPrice = getIntOrDefault(pubInfo, "pubPrice");

            // 3. 諛? ?????⑥??쇰㈃ ??ш?瑜?珥湲?諛?媛濡 怨?
            int currentPrice = pubAmount > 0 ? pubPrice : stockDetailRepository.getStockPrice(stockId);

            // 4. ?댁 媛寃?議고
            int prevPrice = stockDetailRepository.getPervPrice(stockId);

            // 5. ????鍮 蹂???諛 ?깅쎈?
            int priceChange = currentPrice - prevPrice;
            double changeRate = 0.0;
            if (prevPrice != 0) {
                changeRate = (double) priceChange / prevPrice * 100;
                changeRate = Math.round(changeRate * 100.0) / 100.0;
            }

            // 6. StockPriceResponse? ???
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
