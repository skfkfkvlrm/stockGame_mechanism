package com.skfkfkvlrm.stockservice.domain.stock;

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

    public String getStockName() { return stockName; }
    public int getAmount() { return amount; }
    public int getCurrentPrice() { return currentPrice; }
    public int getAveragePrice() { return averagePrice; }
    public int getPurchasePrice() { return purchasePrice; }
    public long getTotalPurchasePrice() { return totalPurchasePrice; }
    public long getProfit() { return profit; }
}
