package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockservice.domain.stock.Order;

import java.util.List;

public interface StockDetailService {
    // 二쇱 湲곕낯 ?蹂?諛 ???吏? 議고
    StockDetailResponse getStockDetailInfo(int stockId);
    // ?깅?? 二쇰Ц ???紐⑸? 議고
    List<Order> getLiveOrderList(int stockId, String type);
    // ???泥 二쇰Ц 紐⑸? 議고
    List<Order> getwaitingOrderList(int stockId, String studentId);
    // ?泥?二쇱 紐⑸? ???議고
    List<StockDetailResponse> getAllStocks();
    // KOSPI / KOSDAQ ???吏? ?곗? 議고
    List<MarketIndexResponse> getMarketIndices();
}
