package com.skfkfkvlrm.adminservice.client;

import com.skfkfkvlrm.adminservice.domain.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "coupon-service")
public interface CouponClient {

    @GetMapping("/api/coupons")
    ApiResponse<List<Map<String, Object>>> getAllCoupons();

    @PostMapping("/api/coupons/admin/coupons")
    ApiResponse<Boolean> createCoupon(@RequestBody Map<String, Object> body);

    @PutMapping("/api/coupons/admin/coupons/{couponId}")
    ApiResponse<Boolean> updateCoupon(@PathVariable("couponId") int couponId, @RequestBody Map<String, Object> body);

    @DeleteMapping("/api/coupons/admin/coupons/{couponId}")
    ApiResponse<Boolean> deleteCoupon(@PathVariable("couponId") int couponId);

    @GetMapping("/api/coupons/admin/students/{studentId}")
    ApiResponse<List<Map<String, Object>>> getStudentCoupons(@PathVariable("studentId") String studentId);
}
