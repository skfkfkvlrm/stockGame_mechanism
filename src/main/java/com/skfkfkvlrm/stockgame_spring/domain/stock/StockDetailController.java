package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockPriceHistory;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockPriceHistoryRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailService;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockDetailController {
    private final StockDetailService stockDetailService;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    @GetMapping("")
    public ApiResponse<java.util.List<StockDetailResponse>> getStockList() {
        java.util.List<StockDetailResponse> list = stockDetailService.getAllStocks();
        return ApiResponse.success("Stock list", list);
    }

    @GetMapping("/market-index")
    public ApiResponse<java.util.List<MarketIndexResponse>> getMarketIndices() {
        java.util.List<MarketIndexResponse> indices = stockDetailService.getMarketIndices();
        return ApiResponse.success("Market indices", indices);
    }

    @GetMapping("/{stockId}")
    public ApiResponse<StockDetailResponse> getStockDetail(
            @PathVariable("stockId") int stockId,
            @org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        
        if (studentId == null) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        
        StockDetailResponse response = stockDetailService.getStockDetailInfo(stockId);
        return ApiResponse.success("Stock details", response);
    }

    @GetMapping("/{stockId}/history")
    public ApiResponse<java.util.List<StockPriceHistory>> getStockHistory(
            @PathVariable("stockId") int stockId) {
        java.util.List<StockPriceHistory> history = stockPriceHistoryRepository.findHistoryByStockId(stockId);
        return ApiResponse.success("Stock price history", history);
    }

    @GetMapping("/{stockId}/orderbook")
    public ApiResponse<java.util.Map<String, java.util.List<Order>>> getOrderbook(
            @PathVariable("stockId") int stockId) {
        java.util.List<Order> sellOrders = stockDetailService.getLiveOrderList(stockId, "매도");
        java.util.List<Order> buyOrders = stockDetailService.getLiveOrderList(stockId, "매수");
        
        java.util.Map<String, java.util.List<Order>> orderbook = new java.util.HashMap<>();
        orderbook.put("sell", sellOrders);
        orderbook.put("buy", buyOrders);
        
        return ApiResponse.success("Orderbook", orderbook);
    }

    @GetMapping("/{stockId}/orders/my")
    public ApiResponse<java.util.List<Order>> getMyOrders(
            @PathVariable("stockId") int stockId,
            @org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        java.util.List<Order> myOrders = stockDetailService.getwaitingOrderList(stockId, studentId);
        return ApiResponse.success("My Orders", myOrders);
    }
}
