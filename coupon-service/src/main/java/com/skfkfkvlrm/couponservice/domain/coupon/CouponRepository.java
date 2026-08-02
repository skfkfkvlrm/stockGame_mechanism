package com.skfkfkvlrm.couponservice.domain.coupon;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CouponRepository {
    List<Coupon> getCouponList();
    List<CouponPurchase> getMyCouponList(@Param("studentId") String studentId);
    void insertCouponPurchase(@Param("studentId") String studentId, @Param("couponId") int couponId, @Param("status") String status);
    void updateCouponPurchaseStatus(@Param("couponPurchaseId") int couponPurchaseId, @Param("status") String status);
}
