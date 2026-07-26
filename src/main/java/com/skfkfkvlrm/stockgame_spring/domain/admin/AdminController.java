package com.skfkfkvlrm.stockgame_spring.domain.admin;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettingsRepository;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
