package com.skfkfkvlrm.stockservice.domain.stock;

public interface StockOrderService {
    String buyStock(StockOrderRequest request);
    String sellStock(StockOrderRequest request);
    int cancelOrder(int orderId, String studentId);
}
