package com.skfkfkvlrm.stockservice.domain.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
