package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockOrderResponse {
    private int orderId;
    private OrderStatus content;
    private OrderStatus state;
    private int price;
    private int amount;
    private String studentId;
    private int stockId;
    private LocalDateTime createdDate;
}
