package com.skfkfkvlrm.couponservice.domain.coupon;

public interface CouponService {
    boolean buyCoupon(String studentId, int price, String name, int couponId);
    boolean useCoupon(int couponPurchaseId, String studentId);
}
