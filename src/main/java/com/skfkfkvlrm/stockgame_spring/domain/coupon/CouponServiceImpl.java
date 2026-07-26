package com.skfkfkvlrm.stockgame_spring.domain.coupon;

import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponPurchase;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponPurchaseStatus;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponRepository;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;

    @Override
    public List<Coupon> getCouponList() {
        return couponRepository.getCouponList();
    }

    @Override
    public List<CouponPurchase> getMyCouponList(String studentId) {
        return couponRepository.getMyCouponList(studentId);
    }

    @Override
    @Transactional
    public void buyCoupon(String studentId, int couponPrice, String couponName, int couponId) {
        // 1. 포인트 잔액 확인
        int currentPoints = couponRepository.getStudentPoint(studentId);
        if (currentPoints < couponPrice) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InsufficientPointException();
        }
        // 2. 포인트 차감 및 학생 보유 쿠폰 수량 1 증가
        int updatedRows = couponRepository.setStudentAssets(studentId, couponPrice);
        if (updatedRows == 0) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InsufficientPointException();
        }
        // 3. 쿠폰 구매 정보 구매내역에 추가
        couponRepository.setPurchaseRecord(studentId, couponId, couponName, couponPrice, CouponPurchaseStatus.사용전);
    }
}
