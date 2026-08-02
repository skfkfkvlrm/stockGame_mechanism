package com.skfkfkvlrm.stockgame_spring.domain.admin;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettingsRepository;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MarketSettingsRepository marketSettingsRepository;
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, String>> dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        Map<String, String> data = new HashMap<>();
        data.put("username", authentication.getName());
        data.put("role", authentication.getAuthorities().iterator().next().getAuthority());
        return ApiResponse.success("Dashboard data", data);
    }

    @GetMapping("/students")
    public ApiResponse<List<StudentAdminResponse>> students() {
        return ApiResponse.success("Student list", adminService.getAllStudents());
    }

    @GetMapping("/stocks")
    public ApiResponse<List<Stock>> stocks() {
        return ApiResponse.success("Stock list", adminService.getAllStocks());
    }

    @GetMapping("/coupons")
    public ApiResponse<List<Coupon>> coupons() {
        return ApiResponse.success("Coupon list", adminService.getAllCoupons());
    }

    @GetMapping("/market/status")
    public ApiResponse<Map<String, Object>> getMarketStatus() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings != null && settings.isMarketOpen());
        return ApiResponse.success("Market status", data);
    }

    @PostMapping("/market/toggle")
    public ApiResponse<Map<String, Object>> toggleMarketStatus() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null) {
            settings = MarketSettings.builder().id(1).marketOpen(true).dailyTradeLimit(0).build();
        } else {
            settings.setMarketOpen(!settings.isMarketOpen());
        }
        marketSettingsRepository.save(settings);
        
        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings.isMarketOpen());
        return ApiResponse.success("Market status toggled", data);
    }

    @PostMapping("/students/{studentId}/point")
    public ApiResponse<Boolean> adjustPoint(
            @org.springframework.web.bind.annotation.PathVariable("studentId") String studentId,
            @org.springframework.web.bind.annotation.RequestBody PointAdjustmentRequest request) {
        adminService.adjustStudentPoint(studentId, request.getAmount(), request.getReason());
        return ApiResponse.success("학생 포인트가 성공적으로 반영되었습니다.", true);
    }

    @GetMapping("/students/{studentId}/detail")
    public ApiResponse<Map<String, Object>> getStudentDetail(
            @org.springframework.web.bind.annotation.PathVariable("studentId") String studentId) {
        return ApiResponse.success("Student detail info", adminService.getStudentDetail(studentId));
    }

    @PostMapping("/stocks")
    public ApiResponse<Boolean> createStock(@org.springframework.web.bind.annotation.RequestBody StockRequest request) {
        adminService.createStock(request);
        return ApiResponse.success("신규 주식 종목이 성공적으로 상장되었습니다.", true);
    }

    @PutMapping("/stocks/{stockId}")
    public ApiResponse<Boolean> updateStock(
            @org.springframework.web.bind.annotation.PathVariable("stockId") int stockId,
            @org.springframework.web.bind.annotation.RequestBody StockRequest request) {
        adminService.updateStock(stockId, request);
        return ApiResponse.success("주식 종목 정보가 성공적으로 수정되었습니다.", true);
    }

    @DeleteMapping("/stocks/{stockId}")
    public ApiResponse<Boolean> deleteStock(@org.springframework.web.bind.annotation.PathVariable("stockId") int stockId) {
        adminService.deleteStock(stockId);
        return ApiResponse.success("주식 종목이 성공적으로 상장폐지(삭제)되었습니다.", true);
    }

    @PostMapping("/coupons")
    public ApiResponse<Boolean> createCoupon(@org.springframework.web.bind.annotation.RequestBody CouponRequest request) {
        adminService.createCoupon(request);
        return ApiResponse.success("신규 쿠폰 상품이 성공적으로 등록되었습니다.", true);
    }

    @PutMapping("/coupons/{couponId}")
    public ApiResponse<Boolean> updateCoupon(
            @org.springframework.web.bind.annotation.PathVariable("couponId") int couponId,
            @org.springframework.web.bind.annotation.RequestBody CouponRequest request) {
        adminService.updateCoupon(couponId, request);
        return ApiResponse.success("쿠폰 상품 정보가 성공적으로 수정되었습니다.", true);
    }

    @DeleteMapping("/coupons/{couponId}")
    public ApiResponse<Boolean> deleteCoupon(@org.springframework.web.bind.annotation.PathVariable("couponId") int couponId) {
        adminService.deleteCoupon(couponId);
        return ApiResponse.success("쿠폰 상품이 성공적으로 삭제되었습니다.", true);
    }
}
