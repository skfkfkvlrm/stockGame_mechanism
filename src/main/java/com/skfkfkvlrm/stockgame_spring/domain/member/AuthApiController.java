package com.skfkfkvlrm.stockgame_spring.domain.member;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @GetMapping("/status")
    public Map<String, Object> status(
            @org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        
        Map<String, Object> response = new HashMap<>();

        // 1. 관리자 권한 확인 (Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            response.put("isAuthenticated", true);
            response.put("username", auth.getName());
            // Safe fallback if no authority
            if (!auth.getAuthorities().isEmpty()) {
                response.put("role", auth.getAuthorities().iterator().next().getAuthority());
            } else {
                response.put("role", "ROLE_STUDENT");
            }
            return response;
        }

        // 2. 학생 권한 확인 (JWT via RequestAttribute)
        if (studentId != null) {
            response.put("isAuthenticated", true);
            response.put("username", studentId);
            response.put("role", "ROLE_STUDENT");
            return response;
        }

        // 3. 비로그인 상태
        response.put("isAuthenticated", false);
        return response;
    }
}
