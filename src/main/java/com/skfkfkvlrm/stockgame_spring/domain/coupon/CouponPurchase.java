package com.skfkfkvlrm.stockgame_spring.domain.coupon;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "coupon_purchase")
public class CouponPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int couponPurchaseId;
    private int studentId;
    private int couponId;
    private int price;
    private String name;
    @Enumerated(EnumType.STRING)
    private CouponPurchaseStatus state;
    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
