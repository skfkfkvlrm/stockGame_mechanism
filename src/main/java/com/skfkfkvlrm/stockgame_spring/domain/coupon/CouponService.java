package com.skfkfkvlrm.stockgame_spring.domain.coupon;

import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponPurchase;

import java.util.List;

public interface CouponService {
    //등록된 쿠폰 목록 조회
    List<Coupon> getCouponList();

    //특정 학생의 쿠폰 목록 조회
    List<CouponPurchase> getMyCouponList(String studentId);

    //쿠폰 구매
    void buyCoupon(String studentId, int couponPrice, String couponName, int couponId);
}
