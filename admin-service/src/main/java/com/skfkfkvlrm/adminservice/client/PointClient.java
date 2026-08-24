package com.skfkfkvlrm.adminservice.client;

import com.skfkfkvlrm.adminservice.domain.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "point-service")
public interface PointClient {
    @GetMapping("/api/internal/points/{studentId}")
    ApiResponse<Integer> getStudentPoint(@PathVariable("studentId") String studentId);

    @PostMapping("/api/internal/points/{studentId}/increase")
    ApiResponse<Boolean> increasePoint(@PathVariable("studentId") String studentId, @RequestParam("amount") int amount);

    @PostMapping("/api/internal/points/{studentId}/decrease")
    ApiResponse<Boolean> decreasePoint(@PathVariable("studentId") String studentId, @RequestParam("amount") int amount);

    @GetMapping("/api/history/admin/{studentId}")
    ApiResponse<java.util.List<Object>> getStudentPointHistory(@PathVariable("studentId") String studentId);
}
