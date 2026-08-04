package com.skfkfkvlrm.pointservice.client;

import com.skfkfkvlrm.pointservice.domain.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "stock-service")
public interface StockClient {
    @GetMapping("/api/stock/{stockId}/price")
    ApiResponse<Integer> getStockPrice(@PathVariable("stockId") int stockId);
}
