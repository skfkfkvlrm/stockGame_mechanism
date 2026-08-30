package com.skfkfkvlrm.adminservice.domain.admin;

import com.skfkfkvlrm.adminservice.client.MemberClient;
import com.skfkfkvlrm.adminservice.client.PointClient;
import com.skfkfkvlrm.adminservice.client.StockClient;
import com.skfkfkvlrm.adminservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final MarketSettingsRepository marketSettingsRepository;
    private final StockClient stockClient;
    private final MemberClient memberClient;
    private final PointClient pointClient;
    private final com.skfkfkvlrm.adminservice.client.CouponClient couponClient;

    // --- 시장 개장 / 휴장 상태 관리 ---
    @GetMapping("/market/status")
    public ApiResponse<Map<String, Object>> getMarketStatus() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        Map<String, Object> data = new HashMap<>();
        if (settings == null) {
            data.put("marketOpen", true);
            data.put("mode", "AUTO");
            data.put("openTime", "09:00");
            data.put("closeTime", "15:30");
            data.put("statusCode", "OPEN");
        } else {
            boolean isOpen = settings.calculateIsMarketOpen();
            String status = settings.calculateStatusCode();
            data.put("marketOpen", isOpen);
            data.put("mode", settings.getMode());
            data.put("openTime", settings.getOpenTime());
            data.put("closeTime", settings.getCloseTime());
            data.put("operatingDays", settings.getOperatingDays());
            data.put("statusCode", status);
        }
        return ApiResponse.success("Market status fetched", data);
    }

    @PostMapping("/market/toggle")
    public ApiResponse<Map<String, Object>> toggleMarketStatus() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null) {
            settings = MarketSettings.builder()
                    .id(1)
                    .isMarketOpen(false)
                    .mode("MANUAL")
                    .openTime("09:00")
                    .closeTime("15:30")
                    .statusCode("MANUAL_PAUSE")
                    .build();
        } else {
            settings.setMode("MANUAL");
            settings.setMarketOpen(!settings.isMarketOpen());
            settings.setStatusCode(settings.isMarketOpen() ? "OPEN" : "MANUAL_PAUSE");
        }
        marketSettingsRepository.save(settings);

        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings.isMarketOpen());
        data.put("mode", settings.getMode());
        data.put("openTime", settings.getOpenTime());
        data.put("closeTime", settings.getCloseTime());
        data.put("statusCode", settings.getStatusCode());
        return ApiResponse.success("Market status toggled", data);
    }

    @PutMapping("/market/settings")
    public ApiResponse<Map<String, Object>> updateMarketSettings(@RequestBody Map<String, Object> body) {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null) {
            settings = MarketSettings.builder().id(1).build();
        }
        if (body.containsKey("mode")) settings.setMode(String.valueOf(body.get("mode")));
        if (body.containsKey("openTime")) settings.setOpenTime(String.valueOf(body.get("openTime")));
        if (body.containsKey("closeTime")) settings.setCloseTime(String.valueOf(body.get("closeTime")));
        if (body.containsKey("marketOpen")) settings.setMarketOpen(Boolean.parseBoolean(String.valueOf(body.get("marketOpen"))));

        settings.setStatusCode(settings.calculateStatusCode());
        marketSettingsRepository.save(settings);

        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings.calculateIsMarketOpen());
        data.put("mode", settings.getMode());
        data.put("openTime", settings.getOpenTime());
        data.put("closeTime", settings.getCloseTime());
        data.put("statusCode", settings.calculateStatusCode());
        return ApiResponse.success("Market settings updated", data);
    }

    // --- 주식 종목 상장 / 수정 / 상장폐지 (Stock-Service 위임) ---
    @PostMapping("/stocks")
    public ApiResponse<Boolean> createStock(@RequestBody StockAdminRequest request) {
        return stockClient.createStock(request);
    }

    @PutMapping("/stocks/{stockId}")
    public ApiResponse<Boolean> updateStock(@PathVariable("stockId") int stockId, @RequestBody StockAdminRequest request) {
        return stockClient.updateStock(stockId, request);
    }

    @DeleteMapping("/stocks/{stockId}")
    public ApiResponse<Boolean> deleteStock(
            @PathVariable("stockId") int stockId,
            @RequestParam(value = "compensationPrice", defaultValue = "0") int compensationPrice,
            @RequestParam(value = "reason", required = false) String reason) {
        return stockClient.deleteStock(stockId, compensationPrice, reason);
    }

    @GetMapping("/stocks/{stockId}/transactions")
    public ApiResponse<List<Object>> getStockTransactions(@PathVariable("stockId") int stockId) {
        return stockClient.getStockTransactions(stockId);
    }

    // --- 학생 계정 및 포인트 관리 (Member-Service & Point-Service 위임) ---
    @GetMapping("/students")
    public ApiResponse<List<Map<String, Object>>> getStudents() {
        return memberClient.getStudentRanking();
    }

    @PostMapping("/students/{studentId}/point")
    public ApiResponse<Boolean> adjustStudentPoint(@PathVariable("studentId") String studentId, @RequestBody Map<String, Object> body) {
        return memberClient.adjustStudentPoint(studentId, body);
    }

    @DeleteMapping("/students/{studentId}")
    public ApiResponse<Boolean> deleteStudent(@PathVariable("studentId") String studentId) {
        ApiResponse<Boolean> response = memberClient.deleteStudent(studentId);
        if (response != null && Boolean.TRUE.equals(response.getData())) {
            try {
                stockClient.liquidateStudentAssets(studentId);
            } catch (Exception e) {
                // Ignore exception to let the deletion process complete, but log it
                System.err.println("Failed to liquidate assets for deleted student: " + studentId);
            }
        }
        return response;
    }

    @GetMapping("/students/{studentId}/points")
    public ApiResponse<List<Object>> getStudentPointHistory(@PathVariable("studentId") String studentId) {
        return pointClient.getStudentPointHistory(studentId);
    }

    @GetMapping("/students/{studentId}/coupons")
    public ApiResponse<List<Map<String, Object>>> getStudentCoupons(@PathVariable("studentId") String studentId) {
        return couponClient.getStudentCoupons(studentId);
    }

    // --- 쿠폰 상품 관리 (Coupon-Service 위임) ---
    @GetMapping("/coupons")
    public ApiResponse<List<Map<String, Object>>> getCoupons() {
        return couponClient.getAllCoupons();
    }

    @PostMapping("/coupons")
    public ApiResponse<Boolean> createCoupon(@RequestBody Map<String, Object> body) {
        return couponClient.createCoupon(body);
    }

    @PutMapping("/coupons/{couponId}")
    public ApiResponse<Boolean> updateCoupon(@PathVariable("couponId") int couponId, @RequestBody Map<String, Object> body) {
        return couponClient.updateCoupon(couponId, body);
    }

    @DeleteMapping("/coupons/{couponId}")
    public ApiResponse<Boolean> deleteCoupon(@PathVariable("couponId") int couponId) {
        return couponClient.deleteCoupon(couponId);
    }
}
