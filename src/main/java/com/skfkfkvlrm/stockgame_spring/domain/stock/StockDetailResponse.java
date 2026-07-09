package com.skfkfkvlrm.stockgame_spring.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockDetailResponse {
    private int stockId;
    private String stockName;
    private String content;
    private int nowPrice;
    private int prevPrice;
    private int pubPrice;
    private int pubAmount;
}
