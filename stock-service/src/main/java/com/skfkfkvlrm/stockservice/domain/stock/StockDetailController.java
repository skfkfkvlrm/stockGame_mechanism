package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.common.ApiResponse;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockservice.domain.stock.StockPriceHistory;
import com.skfkfkvlrm.stockservice.domain.stock.StockPriceHistoryRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailService;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/stock", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class StockDetailController {
    private final StockDetailService stockDetailService;
    private final StockDetailRepository stockDetailRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final StockListRepository stockListRepository;
    private final com.skfkfkvlrm.stockservice.domain.admin.MarketSettingsRepository marketSettingsRepository;
    private final ClosingAuctionService closingAuctionService;

    @GetMapping("")
    public ApiResponse<List<StockDetailResponse>> getStockList() {
        List<StockDetailResponse> list = stockDetailService.getAllStocks();
        return ApiResponse.success("Stock list", list);
    }

    @GetMapping("/market-index")
    public ApiResponse<List<MarketIndexResponse>> getMarketIndices() {
        List<MarketIndexResponse> indices = stockDetailService.getMarketIndices();
        return ApiResponse.success("Market indices", indices);
    }

    @GetMapping("/{stockId}")
    public ApiResponse<StockDetailResponse> getStockDetail(
            @PathVariable("stockId") int stockId,
            @RequestAttribute(name = "studentId", required = false) String studentId) {
        
        StockDetailResponse response = stockDetailService.getStockDetailInfo(stockId);
        return ApiResponse.success("Stock details", response);
    }

    @GetMapping("/{stockId}/history")
    public ApiResponse<List<StockPriceHistory>> getStockHistory(
            @PathVariable("stockId") int stockId) {
        List<StockPriceHistory> history = stockPriceHistoryRepository.findHistoryByStockId(stockId);
        return ApiResponse.success("Stock price history", history);
    }

    @GetMapping("/{stockId}/orderbook")
    public ApiResponse<Map<String, List<Order>>> getOrderbook(
            @PathVariable("stockId") int stockId) {
        List<Order> sellOrders = stockDetailService.getLiveOrderList(stockId, "매도");
        List<Order> buyOrders = stockDetailService.getLiveOrderList(stockId, "매수");
        
        Map<String, List<Order>> orderbook = new HashMap<>();
        orderbook.put("sell", sellOrders);
        orderbook.put("buy", buyOrders);
        
        return ApiResponse.success("Orderbook", orderbook);
    }

    @GetMapping("/{stockId}/orders/my")
    public ApiResponse<List<Order>> getMyOrders(
            @PathVariable("stockId") int stockId,
            @RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<Order> myOrders = stockDetailService.getwaitingOrderList(stockId, studentId);
        return ApiResponse.success("My Orders", myOrders);
    }

    @PostMapping("/admin/stocks")
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<Boolean> createStockAdmin(@RequestBody com.skfkfkvlrm.stockservice.domain.admin.StockRequest request) {
        stockListRepository.insertStock(request);
        
        // [Step 1-B: LP 초기 지정가 매도 주문 자동 배치]
        if (request.getPublicationBalance() > 0 && request.getPublicationPrice() > 0) {
            Order lpSellOrder = Order.builder()
                    .stockId(request.getStockId())
                    .studentId("SYSTEM_LP")
                    .content(OrderStatus.SELL)
                    .price(request.getPublicationPrice())
                    .amount(request.getPublicationBalance())
                    .state(OrderStatus.WAITING)
                    .build();
            stockDetailRepository.insertOrder(lpSellOrder);
        }
        
        return ApiResponse.success("신규 주식 종목이 상장되었으며, LP 초기 매도 물량이 호가창에 배치되었습니다.", true);
    }

    @PutMapping("/admin/stocks/{stockId}")
    public ApiResponse<Boolean> updateStockAdmin(@PathVariable("stockId") int stockId, @RequestBody com.skfkfkvlrm.stockservice.domain.admin.StockRequest request) {
        stockListRepository.updateStock(stockId, request);
        return ApiResponse.success("주식 종목 정보가 수정되었습니다.", true);
    }

    @DeleteMapping("/admin/stocks/{stockId}")
    public ApiResponse<Boolean> deleteStockAdmin(
            @PathVariable("stockId") int stockId,
            @RequestParam(value = "compensationPrice", defaultValue = "0") int compensationPrice,
            @RequestParam(value = "reason", required = false) String reason) {
        
        stockDetailService.delistStock(stockId, compensationPrice, reason);
        return ApiResponse.success("주식 종목이 상장폐지되었으며, 대기 주문 취소 및 보유 주식 청산이 완료되었습니다.", true);
    }

    @GetMapping("/admin/stocks/{stockId}/transactions")
    public ApiResponse<List<Map<String, Object>>> getStockTransactionsAdmin(@PathVariable("stockId") int stockId) {
        List<Map<String, Object>> transactions = stockDetailRepository.getStockTransactionsByStockId(stockId);
        return ApiResponse.success("Stock transactions fetched", transactions);
    }

    @GetMapping({"/market/status", "/admin/market/status"})
    public ApiResponse<Map<String, Object>> getMarketStatus() {
        com.skfkfkvlrm.stockservice.domain.admin.MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        Map<String, Object> data = new HashMap<>();
        if (settings == null) {
            data.put("marketOpen", true);
            data.put("mode", "AUTO");
            data.put("openTime", "09:00");
            data.put("closeTime", "15:30");
            data.put("callAuctionStartTime", "15:20");
            data.put("statusCode", "OPEN");
        } else {
            boolean isOpen = settings.calculateIsMarketOpen();
            String status = settings.calculateStatusCode();
            data.put("marketOpen", isOpen);
            data.put("mode", settings.getMode());
            data.put("openTime", settings.getOpenTime());
            data.put("closeTime", settings.getCloseTime());
            data.put("callAuctionStartTime", settings.getCallAuctionStartTime());
            data.put("operatingDays", settings.getOperatingDays());
            data.put("statusCode", status);
        }
        return ApiResponse.success("Market status", data);
    }

    @PostMapping("/admin/market/toggle")
    public ApiResponse<Map<String, Object>> toggleMarketStatus() {
        com.skfkfkvlrm.stockservice.domain.admin.MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null) {
            settings = com.skfkfkvlrm.stockservice.domain.admin.MarketSettings.builder()
                    .id(1)
                    .isMarketOpen(false)
                    .mode("MANUAL")
                    .openTime("09:00")
                    .closeTime("15:30")
                    .callAuctionStartTime("15:20")
                    .statusCode("MANUAL_PAUSE")
                    .build();
        } else {
            settings.setMode("MANUAL");
            settings.setMarketOpen(!settings.isMarketOpen());
            settings.setStatusCode(settings.isMarketOpen() ? "OPEN" : "MANUAL_PAUSE");
        }
        marketSettingsRepository.save(settings);

        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings.isMarketOpen());
        data.put("mode", settings.getMode());
        data.put("openTime", settings.getOpenTime());
        data.put("closeTime", settings.getCloseTime());
        data.put("callAuctionStartTime", settings.getCallAuctionStartTime());
        data.put("statusCode", settings.getStatusCode());
        return ApiResponse.success("Market status toggled", data);
    }

    @PutMapping("/admin/market/settings")
    public ApiResponse<Map<String, Object>> updateMarketSettings(@RequestBody Map<String, Object> body) {
        com.skfkfkvlrm.stockservice.domain.admin.MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null) {
            settings = com.skfkfkvlrm.stockservice.domain.admin.MarketSettings.builder().id(1).build();
        }
        if (body.containsKey("mode")) settings.setMode(String.valueOf(body.get("mode")));
        if (body.containsKey("openTime")) settings.setOpenTime(String.valueOf(body.get("openTime")));
        if (body.containsKey("closeTime")) settings.setCloseTime(String.valueOf(body.get("closeTime")));
        if (body.containsKey("callAuctionStartTime")) settings.setCallAuctionStartTime(String.valueOf(body.get("callAuctionStartTime")));
        if (body.containsKey("marketOpen")) settings.setMarketOpen(Boolean.parseBoolean(String.valueOf(body.get("marketOpen"))));

        settings.setStatusCode(settings.calculateStatusCode());
        marketSettingsRepository.save(settings);

        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings.calculateIsMarketOpen());
        data.put("mode", settings.getMode());
        data.put("openTime", settings.getOpenTime());
        data.put("closeTime", settings.getCloseTime());
        data.put("callAuctionStartTime", settings.getCallAuctionStartTime());
        data.put("statusCode", settings.calculateStatusCode());
        return ApiResponse.success("Market settings updated", data);
    }

    @PostMapping("/admin/market/execute-closing-auction")
    public ApiResponse<Map<String, Object>> executeClosingAuctionManually() {
        Map<String, Object> result = closingAuctionService.executeClosingAuctionForAllStocks();
        return ApiResponse.success("장 마감 동시호가 단일가 일괄 체결이 실행되었습니다.", result);
    }
}
