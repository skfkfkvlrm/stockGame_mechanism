package com.skfkfkvlrm.couponservice.domain.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponPurchase {
    private int couponPurchaseId;
    private String studentId;
    private int couponId;
    private String name;
    private int price;
    private String state;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime purchasedAt;
}
