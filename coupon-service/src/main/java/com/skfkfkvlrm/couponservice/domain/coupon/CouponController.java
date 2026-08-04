package com.skfkfkvlrm.couponservice.domain.coupon;

import com.skfkfkvlrm.couponservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/coupons", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class CouponController {

    private final CouponRepository couponRepository;
    private final CouponService couponService;

    @GetMapping
    public ApiResponse<List<Coupon>> getCoupons(@RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<Coupon> coupons = couponRepository.getCouponList();
        return ApiResponse.success("Coupon data", coupons);
    }

    @GetMapping("/my")
    public ApiResponse<List<CouponPurchase>> getMyCoupons(@RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<CouponPurchase> coupons = couponRepository.getMyCouponList(studentId);
        return ApiResponse.success("My Coupons", coupons);
    }

    @PostMapping("/{couponId}/buy")
    public ApiResponse<String> buyCoupon(@RequestAttribute(name = "studentId", required = false) String studentId,
                                         @PathVariable("couponId") int couponId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        
        List<Coupon> allCoupons = couponRepository.getCouponList();
        Coupon targetCoupon = allCoupons.stream()
                .filter(c -> c.getCouponId() == couponId)
                .findFirst()
                .orElse(null);
                
        if (targetCoupon == null) {
            return ApiResponse.error("존재하지 않는 쿠폰입니다.");
        }

        if (targetCoupon.getStatus() != null && !"ON_SALE".equalsIgnoreCase(targetCoupon.getStatus())) {
            if ("PAUSED".equalsIgnoreCase(targetCoupon.getStatus())) {
                return ApiResponse.error("해당 쿠폰은 현재 판매가 일시중지되었습니다.");
            } else if ("SOLD_OUT".equalsIgnoreCase(targetCoupon.getStatus())) {
                return ApiResponse.error("해당 쿠폰은 품절/마감되었습니다.");
            } else {
                return ApiResponse.error("현재 구매할 수 없는 쿠폰 상태입니다.");
            }
        }

        try {
            couponService.buyCoupon(studentId, targetCoupon.getPrice(), targetCoupon.getName(), couponId);
            return ApiResponse.success("Coupon bought", "쿠폰 구매에 성공했습니다.");
        } catch (Exception e) {
            return ApiResponse.error("포인트가 부족하거나 쿠폰 구매 중 오류가 발생했습니다.");
        }
    }

    @PatchMapping("/{purchaseId}/use")
    public ApiResponse<String> useCoupon(
            @RequestAttribute(name = "studentId", required = false) String studentId,
            @PathVariable("purchaseId") int couponPurchaseId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        try {
            couponService.useCoupon(couponPurchaseId, studentId);
            return ApiResponse.success("Coupon used", "쿠폰 사용이 완료되었습니다.");
        } catch (Exception e) {
            return ApiResponse.error("쿠폰 사용 처리 중 오류가 발생했습니다.");
        }
    }
}
