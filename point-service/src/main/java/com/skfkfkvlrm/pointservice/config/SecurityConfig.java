package com.skfkfkvlrm.pointservice.config;

import com.skfkfkvlrm.pointservice.auth.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // admin 경로: 관리자 JWT(ROLE_ADMIN) 필수 — admin-service Feign Token Relay로 전파됨(PLAN-SEC-01)
                .requestMatchers("/api/history/admin/**").hasRole("ADMIN")
                // /api/internal/points/** : Gateway application.yaml 라우팅 미등록 원칙 (외부 비노출)
                // coupon-service 등 서비스 간 내부 Feign 호출 전용 — Gateway에 절대 라우팅 추가 금지
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
