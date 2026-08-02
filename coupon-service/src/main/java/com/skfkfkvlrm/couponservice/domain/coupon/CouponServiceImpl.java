package com.skfkfkvlrm.couponservice.domain.coupon;

import com.skfkfkvlrm.couponservice.client.PointClient;
import com.skfkfkvlrm.couponservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final PointClient pointClient;

    @Override
    @Transactional
    public boolean buyCoupon(String studentId, int price, String name, int couponId) {
        ApiResponse<Integer> pointRes = pointClient.getStudentPoint(studentId);
        int currentPoint = (pointRes != null && pointRes.getData() != null) ? pointRes.getData() : 0;

        if (currentPoint < price) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // OpenFeign 포인트 차감
        pointClient.decreasePoint(studentId, price);

        couponRepository.insertCouponPurchase(studentId, couponId, CouponPurchaseStatus.UNUSED.name());
        return true;
    }

    @Override
    @Transactional
    public boolean useCoupon(int couponPurchaseId, String studentId) {
        couponRepository.updateCouponPurchaseStatus(couponPurchaseId, CouponPurchaseStatus.USED.name());
        return true;
    }
}
