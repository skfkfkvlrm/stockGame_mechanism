package com.skfkfkvlrm.stockservice.domain.stock;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int stockId;
    private String name;
    private String content;
    private int publicationBalance;
    private int publicationPrice;
    private int prevPrice;
    private Integer refPrice;
    private String marketStatus = "CONTINUOUS"; // CONTINUOUS, STATIC_VI
    private String status = "LISTED"; // LISTED, SUSPENDED, DELISTED
    @CreationTimestamp
    private LocalDateTime createdDate;

    public Stock() {}

    public Stock(int stockId, String name, String content, int publicationBalance, int publicationPrice, int prevPrice, Integer refPrice, String marketStatus, String status, LocalDateTime createdDate) {
        this.stockId = stockId;
        this.name = name;
        this.content = content;
        this.publicationBalance = publicationBalance;
        this.publicationPrice = publicationPrice;
        this.prevPrice = prevPrice;
        this.refPrice = refPrice;
        this.marketStatus = marketStatus;
        this.status = status;
        this.createdDate = createdDate;
    }

    public static StockBuilder builder() {
        return new StockBuilder();
    }

    public static class StockBuilder {
        private int stockId;
        private String name;
        private String content;
        private int publicationBalance;
        private int publicationPrice;
        private int prevPrice;
        private Integer refPrice;
        private String marketStatus = "CONTINUOUS";
        private String status = "LISTED";
        private LocalDateTime createdDate;

        public StockBuilder stockId(int stockId) { this.stockId = stockId; return this; }
        public StockBuilder name(String name) { this.name = name; return this; }
        public StockBuilder content(String content) { this.content = content; return this; }
        public StockBuilder publicationBalance(int publicationBalance) { this.publicationBalance = publicationBalance; return this; }
        public StockBuilder publicationPrice(int publicationPrice) { this.publicationPrice = publicationPrice; return this; }
        public StockBuilder prevPrice(int prevPrice) { this.prevPrice = prevPrice; return this; }
        public StockBuilder refPrice(Integer refPrice) { this.refPrice = refPrice; return this; }
        public StockBuilder marketStatus(String marketStatus) { this.marketStatus = marketStatus; return this; }
        public StockBuilder status(String status) { this.status = status; return this; }
        public StockBuilder createdDate(LocalDateTime createdDate) { this.createdDate = createdDate; return this; }

        public Stock build() {
            return new Stock(stockId, name, content, publicationBalance, publicationPrice, prevPrice, refPrice, marketStatus, status, createdDate);
        }
    }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getPublicationBalance() { return publicationBalance; }
    public void setPublicationBalance(int publicationBalance) { this.publicationBalance = publicationBalance; }
    public int getPublicationPrice() { return publicationPrice; }
    public void setPublicationPrice(int publicationPrice) { this.publicationPrice = publicationPrice; }
    public int getPrevPrice() { return prevPrice; }
    public void setPrevPrice(int prevPrice) { this.prevPrice = prevPrice; }
    public Integer getRefPrice() { return refPrice; }
    public void setRefPrice(Integer refPrice) { this.refPrice = refPrice; }
    public String getMarketStatus() { return marketStatus; }
    public void setMarketStatus(String marketStatus) { this.marketStatus = marketStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
