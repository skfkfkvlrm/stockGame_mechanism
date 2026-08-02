package com.skfkfkvlrm.stockgame_spring.domain.coupon;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int couponId;
    private String name;
    private int price;
    private String status = "ON_SALE"; // ON_SALE, PAUSED, SOLD_OUT
    @CreationTimestamp
    private LocalDateTime createdDate;

    public Coupon() {}

    public Coupon(int couponId, String name, int price, String status, LocalDateTime createdDate) {
        this.couponId = couponId;
        this.name = name;
        this.price = price;
        this.status = status;
        this.createdDate = createdDate;
    }

    public static CouponBuilder builder() {
        return new CouponBuilder();
    }

    public static class CouponBuilder {
        private int couponId;
        private String name;
        private int price;
        private String status = "ON_SALE";
        private LocalDateTime createdDate;

        public CouponBuilder couponId(int couponId) { this.couponId = couponId; return this; }
        public CouponBuilder name(String name) { this.name = name; return this; }
        public CouponBuilder price(int price) { this.price = price; return this; }
        public CouponBuilder status(String status) { this.status = status; return this; }
        public CouponBuilder createdDate(LocalDateTime createdDate) { this.createdDate = createdDate; return this; }

        public Coupon build() {
            return new Coupon(couponId, name, price, status, createdDate);
        }
    }

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
