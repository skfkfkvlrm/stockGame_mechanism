package com.skfkfkvlrm.couponservice.domain.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponPurchase {
    private int couponPurchaseId;
    private String studentId;
    private int couponId;
    private String status;
    private LocalDateTime purchasedAt;
}
