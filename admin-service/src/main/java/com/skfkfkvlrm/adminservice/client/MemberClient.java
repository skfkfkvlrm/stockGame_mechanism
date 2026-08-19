package com.skfkfkvlrm.adminservice.client;

import com.skfkfkvlrm.adminservice.domain.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "member-service")
public interface MemberClient {
    @GetMapping("/api/members/ranking")
    ApiResponse<List<Map<String, Object>>> getStudentRanking();

    @PostMapping("/api/members/admin/students/{studentId}/point")
    ApiResponse<Boolean> adjustStudentPoint(@PathVariable("studentId") String studentId, @RequestBody Map<String, Object> body);

    @DeleteMapping("/api/members/admin/students/{studentId}")
    ApiResponse<Boolean> deleteStudent(@PathVariable("studentId") String studentId);
}
