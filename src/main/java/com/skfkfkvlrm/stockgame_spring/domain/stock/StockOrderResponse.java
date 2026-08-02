package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOrderResponse {
    private String studentId;
    private int orderId;
    private int stockId;
    private String content;
    private int price;
    private int amount;
    private OrderStatus state;

    public String getStudentId() { return studentId; }
    public int getOrderId() { return orderId; }
    public int getStockId() { return stockId; }
    public String getContent() { return content; }
    public int getPrice() { return price; }
    public int getAmount() { return amount; }
    public OrderStatus getState() { return state; }
}
