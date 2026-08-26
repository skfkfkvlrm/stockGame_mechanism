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
        ApiResponse<Boolean> decreaseRes = pointClient.decreasePoint(studentId, price);
        if (decreaseRes == null || Boolean.FALSE.equals(decreaseRes.getData())) {
            throw new IllegalStateException("포인트 차감 처리에 실패했습니다.");
        }

        try {
            couponRepository.insertCouponPurchase(studentId, couponId, CouponPurchaseStatus.UNUSED.name());
        } catch (Exception e) {
            // [Saga Compensation] 로컬 DB 인서트 실패 시 원격 포인트 복구
            try {
                pointClient.increasePoint(studentId, price);
            } catch (Exception ex) {
                // 로그 기록 등 보상 실패 방어
                System.err.println("[CRITICAL] Saga Compensation Failed: studentId=" + studentId + ", refundAmount=" + price);
            }
            throw new RuntimeException("쿠폰 발급 중 오류가 발생하여 결제가 취소되었습니다.", e);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean useCoupon(int couponPurchaseId, String studentId) {
        couponRepository.updateCouponPurchaseStatus(couponPurchaseId, CouponPurchaseStatus.USED.name());
        return true;
    }
}
