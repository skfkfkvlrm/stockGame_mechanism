package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.common.ApiResponse;
import com.skfkfkvlrm.stockservice.exception.UnauthorizedAccessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/orders", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class StockOrderController {
    private final StockOrderService stockOrderService;

    @PostMapping("/buy")
    public ApiResponse<String> buyStock(@Valid @RequestBody StockOrderRequest request,
                           @RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            throw new UnauthorizedAccessException();
        }
        request.setStudentId(studentId);
        String result = stockOrderService.buyStock(request);
        return ApiResponse.success(result, result);
    }

    @PostMapping("/sell")
    public ApiResponse<String> sellStock(@Valid @RequestBody StockOrderRequest request,
                            @RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            throw new UnauthorizedAccessException();
        }
        request.setStudentId(studentId);
        String result = stockOrderService.sellStock(request);
        return ApiResponse.success(result, result);
    }

    @PostMapping("/cancel")
    public ApiResponse<String> cancelOrder(@RequestParam("orderId") int orderId, 
                                           @RequestParam("stockId") int stockId,
                              @RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            throw new UnauthorizedAccessException();
        }
        stockOrderService.cancelOrder(orderId, studentId);
        return ApiResponse.success("주문이 취소되었습니다.", "Success");
    }
}
