package com.skfkfkvlrm.stockgame_spring.domain.stock;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;
    @Enumerated(EnumType.STRING)
    private OrderStatus content;
    private int price;
    private int amount;
    @Enumerated(EnumType.STRING)
    private OrderStatus state;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    private LocalDateTime deletedDate;
    private String studentId;
    private int stockId;

    public Order() {}

    public Order(int orderId, OrderStatus content, int price, int amount, OrderStatus state, LocalDateTime createdDate, LocalDateTime updatedDate, LocalDateTime deletedDate, String studentId, int stockId) {
        this.orderId = orderId;
        this.content = content;
        this.price = price;
        this.amount = amount;
        this.state = state;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.deletedDate = deletedDate;
        this.studentId = studentId;
        this.stockId = stockId;
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public static class OrderBuilder {
        private int orderId;
        private OrderStatus content;
        private int price;
        private int amount;
        private OrderStatus state;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;
        private LocalDateTime deletedDate;
        private String studentId;
        private int stockId;

        public OrderBuilder orderId(int orderId) { this.orderId = orderId; return this; }
        public OrderBuilder content(OrderStatus content) { this.content = content; return this; }
        public OrderBuilder price(int price) { this.price = price; return this; }
        public OrderBuilder amount(int amount) { this.amount = amount; return this; }
        public OrderBuilder state(OrderStatus state) { this.state = state; return this; }
        public OrderBuilder createdDate(LocalDateTime createdDate) { this.createdDate = createdDate; return this; }
        public OrderBuilder updatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; return this; }
        public OrderBuilder deletedDate(LocalDateTime deletedDate) { this.deletedDate = deletedDate; return this; }
        public OrderBuilder studentId(String studentId) { this.studentId = studentId; return this; }
        public OrderBuilder stockId(int stockId) { this.stockId = stockId; return this; }

        public Order build() {
            return new Order(orderId, content, price, amount, state, createdDate, updatedDate, deletedDate, studentId, stockId);
        }
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public OrderStatus getContent() { return content; }
    public void setContent(OrderStatus content) { this.content = content; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public OrderStatus getState() { return state; }
    public void setState(OrderStatus state) { this.state = state; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public LocalDateTime getDeletedDate() { return deletedDate; }
    public void setDeletedDate(LocalDateTime deletedDate) { this.deletedDate = deletedDate; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }


    @Override
    public String toString() {
        return "[id=" + orderId + ", content=" + content + ", price=" + price + ", amount=" + amount
                + ", state=" + state + ", createdDate=" + createdDate + "]";
    }
}