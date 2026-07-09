package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.StockOrderRequest;
import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.exception.UnauthorizedAccessException;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class StockOrderController {
    private final StockOrderService stockOrderService;

    @PostMapping("/buy")
    public ApiResponse<String> buyStock(@Valid @RequestBody StockOrderRequest request,
                           @SessionAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            throw new UnauthorizedAccessException();
        }
        request.setStudentId(studentId);
        String result = stockOrderService.buyStock(request);
        if (result.contains("없습니다") || result.contains("부족합니다") || result.contains("필요는 없겠죠")) {
            return ApiResponse.error(result);
        }
        return ApiResponse.success(result, result);
    }

    @PostMapping("/sell")
    public ApiResponse<String> sellStock(@Valid @RequestBody StockOrderRequest request,
                            @SessionAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            throw new UnauthorizedAccessException();
        }
        request.setStudentId(studentId);
        String result = stockOrderService.sellStock(request);
        if (result.contains("없습니다") || result.contains("부족합니다")) {
            return ApiResponse.error(result);
        }
        return ApiResponse.success(result, result);
    }

    @PostMapping("/cancel")
    public ApiResponse<String> cancelOrder(@RequestParam("orderId") int orderId, 
                                           @RequestParam("stockId") int stockId,
                              @SessionAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            throw new UnauthorizedAccessException();
        }
        stockOrderService.cancelOrder(orderId, studentId);
        return ApiResponse.success("주문이 취소되었습니다.", "Success");
    }
}