package com.skfkfkvlrm.stockservice.config;

import com.skfkfkvlrm.stockservice.auth.JwtFilter;
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
                // 시장 운영 상태 조회(개장/휴장 여부)는 학생/관리자 공통 공개 정보이므로 permitAll 허용
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/stock/admin/market/status", "/api/stock/market/status").permitAll()
                // 그 외 종목 CUD(상장/수정/폐지), 장 상태 변경(토글/설정/동시호가실행) 등은 관리자(ROLE_ADMIN) 필수
                .requestMatchers("/api/stock/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
