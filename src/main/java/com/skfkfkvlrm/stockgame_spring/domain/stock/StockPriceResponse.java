package com.skfkfkvlrm.stockgame_spring.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceResponse {
    private int stockId;
    private String stockName;
    private int currentPrice;
    private int prevPrice;
    private int priceChange;
    private double changeRate;

    public int getStockId() { return stockId; }
    public String getStockName() { return stockName; }
    public int getCurrentPrice() { return currentPrice; }
    public int getPrevPrice() { return prevPrice; }
    public int getPriceChange() { return priceChange; }
    public double getChangeRate() { return changeRate; }
}
