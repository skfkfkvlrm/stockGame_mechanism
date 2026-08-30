package com.skfkfkvlrm.adminservice.client;

import com.skfkfkvlrm.adminservice.domain.admin.StockAdminRequest;
import com.skfkfkvlrm.adminservice.domain.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "stock-service")
public interface StockClient {
    @GetMapping("/api/stock")
    ApiResponse<List<Object>> getAllStocks();

    @PostMapping("/api/stock/admin/stocks")
    ApiResponse<Boolean> createStock(@RequestBody StockAdminRequest request);

    @PutMapping("/api/stock/admin/stocks/{stockId}")
    ApiResponse<Boolean> updateStock(@PathVariable("stockId") int stockId, @RequestBody StockAdminRequest request);

    @DeleteMapping("/api/stock/admin/stocks/{stockId}")
    ApiResponse<Boolean> deleteStock(
            @PathVariable("stockId") int stockId, 
            @RequestParam(value = "compensationPrice", defaultValue = "0") int compensationPrice,
            @RequestParam(value = "reason", required = false) String reason);

    @GetMapping("/api/stock/admin/stocks/{stockId}/transactions")
    ApiResponse<List<Object>> getStockTransactions(@PathVariable("stockId") int stockId);
}
