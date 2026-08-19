package com.skfkfkvlrm.adminservice.domain.admin;

import com.skfkfkvlrm.adminservice.client.MemberClient;
import com.skfkfkvlrm.adminservice.client.PointClient;
import com.skfkfkvlrm.adminservice.client.StockClient;
import com.skfkfkvlrm.adminservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MarketSettingsRepository marketSettingsRepository;
    private final StockClient stockClient;
    private final MemberClient memberClient;
    private final PointClient pointClient;

    // --- 시장 개장 / 휴장 상태 관리 ---
    @GetMapping("/market/status")
    public ApiResponse<Map<String, Object>> getMarketStatus() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings == null || settings.isMarketOpen());
        return ApiResponse.success("Market status fetched", data);
    }

    @PostMapping("/market/toggle")
    public ApiResponse<Map<String, Object>> toggleMarketStatus() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings == null) {
            settings = MarketSettings.builder()
                    .id(1)
                    .isMarketOpen(false)
                    .build();
        } else {
            settings.setMarketOpen(!settings.isMarketOpen());
        }
        marketSettingsRepository.save(settings);

        Map<String, Object> data = new HashMap<>();
        data.put("marketOpen", settings.isMarketOpen());
        return ApiResponse.success("Market status toggled", data);
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
    public ApiResponse<Boolean> deleteStock(@PathVariable("stockId") int stockId) {
        return stockClient.deleteStock(stockId);
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
        return memberClient.deleteStudent(studentId);
    }
}
