package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StockOrderRequest {
    @NotNull(message = "주식 번호는 필수입니다.")
    private int stockId;
    private String studentId;
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    private int amount;
    @Min(value = 1, message = "주문 가격은 1 이상이어야 합니다.")
    private int price;
    private String content;
    private OrderStatus state;

    public StockOrderRequest() {}

    public StockOrderRequest(int stockId, String studentId, int amount, int price, String content, OrderStatus state) {
        this.stockId = stockId;
        this.studentId = studentId;
        this.amount = amount;
        this.price = price;
        this.content = content;
        this.state = state;
    }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OrderStatus getState() { return state; }
    public void setState(OrderStatus state) { this.state = state; }
}
