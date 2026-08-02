package com.skfkfkvlrm.stockgame_spring.domain.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "market_settings")
public class MarketSettings {
    @Id
    private Integer id;

    private boolean marketOpen;

    private int dailyTradeLimit;

    public MarketSettings() {}

    public MarketSettings(Integer id, boolean marketOpen, int dailyTradeLimit) {
        this.id = id;
        this.marketOpen = marketOpen;
        this.dailyTradeLimit = dailyTradeLimit;
    }

    public static MarketSettingsBuilder builder() {
        return new MarketSettingsBuilder();
    }

    public static class MarketSettingsBuilder {
        private Integer id;
        private boolean marketOpen;
        private int dailyTradeLimit;

        public MarketSettingsBuilder id(Integer id) { this.id = id; return this; }
        public MarketSettingsBuilder marketOpen(boolean marketOpen) { this.marketOpen = marketOpen; return this; }
        public MarketSettingsBuilder dailyTradeLimit(int dailyTradeLimit) { this.dailyTradeLimit = dailyTradeLimit; return this; }

        public MarketSettings build() {
            return new MarketSettings(id, marketOpen, dailyTradeLimit);
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public boolean isMarketOpen() { return marketOpen; }
    public void setMarketOpen(boolean marketOpen) { this.marketOpen = marketOpen; }
    public int getDailyTradeLimit() { return dailyTradeLimit; }
    public void setDailyTradeLimit(int dailyTradeLimit) { this.dailyTradeLimit = dailyTradeLimit; }
}
