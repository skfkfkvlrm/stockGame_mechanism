package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StockDetailResponse {
    private int stockId;
    private String stockName;
    private String content;
    private int nowPrice;
    private int prevPrice;
    private int pubPrice;
    private int pubAmount;
    private int tradeVolume; // 유저 간 실제 누적 체결 수량 (발행잔량 제외)
    private String status; // 'LISTED' | 'SUSPENDED' | 'DELISTED'

    public StockDetailResponse() {}

    public StockDetailResponse(int stockId, String stockName, String content, int nowPrice, int prevPrice, int pubPrice, int pubAmount, int tradeVolume, String status) {
        this.stockId = stockId;
        this.stockName = stockName;
        this.content = content;
        this.nowPrice = nowPrice;
        this.prevPrice = prevPrice;
        this.pubPrice = pubPrice;
        this.pubAmount = pubAmount;
        this.tradeVolume = tradeVolume;
        this.status = status;
    }

    public int getStockId() { return stockId; }
    public String getStockName() { return stockName; }
    public String getContent() { return content; }
    public int getNowPrice() { return nowPrice; }
    public int getPrevPrice() { return prevPrice; }
    public int getPubPrice() { return pubPrice; }
    public int getPubAmount() { return pubAmount; }
    public int getTradeVolume() { return tradeVolume; }
    public String getStatus() { return status; }
}
