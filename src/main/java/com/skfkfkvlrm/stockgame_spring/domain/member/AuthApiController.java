package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status(
            @org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        
        Map<String, Object> responseData = new HashMap<>();

        // 1. 관리자 권한 확인 (Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            responseData.put("isAuthenticated", true);
            responseData.put("username", auth.getName());
            if (!auth.getAuthorities().isEmpty()) {
                responseData.put("role", auth.getAuthorities().iterator().next().getAuthority());
            } else {
                responseData.put("role", "ROLE_STUDENT");
            }
            return ApiResponse.success("인증 상태 조회 성공", responseData);
        }

        // 2. 학생 및 관리자 권한 확인 (JWT via RequestAttribute)
        if (studentId != null) {
            responseData.put("isAuthenticated", true);
            responseData.put("username", studentId);
            String role = "admin".equals(studentId) ? "ROLE_ADMIN" : ("manager".equals(studentId) ? "ROLE_MANAGER" : "ROLE_STUDENT");
            responseData.put("role", role);
            return ApiResponse.success("인증 상태 조회 성공", responseData);
        }

        // 3. 비로그인 상태
        responseData.put("isAuthenticated", false);
        return ApiResponse.success("비로그인 상태", responseData);
    }
}
