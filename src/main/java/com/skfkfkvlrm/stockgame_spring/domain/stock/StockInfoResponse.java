package com.skfkfkvlrm.stockgame_spring.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockInfoResponse {
    private String stockName;
    private int amount;
    private int currentPrice;
    private int averagePrice;
    private int purchasePrice;
    private long totalPurchasePrice;
    private long profit;
}
