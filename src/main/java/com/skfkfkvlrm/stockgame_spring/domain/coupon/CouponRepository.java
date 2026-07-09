package com.skfkfkvlrm.stockgame_spring.domain.coupon;

import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponPurchase;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponPurchaseStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CouponRepository {
    // 등록된 쿠폰 모두 조회
    List<Coupon> getCouponList();

    // 쿠폰 구매
    String setBuyCoupon(String studentId, int couponPrice, String couponName, CouponPurchaseStatus state, int couponId);

    // 학생이 구매한 쿠폰 개수 조회
    int getMyCouponCount(String studentId);

    // 학생 보유 포인트 조회
    int getStudentPoint(String studentId);

    // 쿠폰 구매 내역 등록
    int setPurchaseRecord(String studentId, int couponId, String couponName, int couponPrice,
            CouponPurchaseStatus state);

    // 학생 포인트 차감 및 보유 쿠폰 수량 증가
    int setStudentAssets(String studentId, int couponPrice);

    // 내가 보유한 쿠폰 조회
    List<CouponPurchase> getMyCouponList(String studentId);
}