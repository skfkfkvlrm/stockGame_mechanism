package com.skfkfkvlrm.stockgame_spring.config;

import com.skfkfkvlrm.stockgame_spring.auth.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 — 이중 FilterChain 구조
 *
 * Chain 1 (@Order(1)): /admin/** 전용
 *   - MANAGER, ADMIN 계정만 접근 가능
 *   - Spring Security Form Login 사용
 *   - AppUserDetailsService (app_users 테이블) 기반 인증
 *
 * Chain 2 (@Order(2)): 나머지 모든 경로
 *   - 전부 permitAll → 기존 학생 HttpSession 방식 그대로 유지
 *   - Spring Security가 학생 인증에 개입하지 않음
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;

    // ── BCrypt 비밀번호 인코더 ───────────────────────────────────────────────────
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── DaoAuthenticationProvider: AppUserDetailsService + BCrypt 연결 ──────────
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // ── Chain 1: /admin/** 전용 FilterChain ────────────────────────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")       // 이 Chain은 /admin/** 경로에만 적용
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()                     // 로그인 페이지는 공개
                .requestMatchers("/admin/stocks/**").hasRole("ADMIN")            // 종목 관리: ADMIN 전용
                .requestMatchers("/admin/**").hasAnyRole("MANAGER", "ADMIN")     // 나머지 관리 경로: MANAGER+ADMIN
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login-process")
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(200);
                    response.getWriter().write("{\"success\":true, \"message\":\"로그인 성공\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"success\":false, \"message\":\"로그인 실패\"}");
                })
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(200);
                    response.getWriter().write("{\"success\":true, \"message\":\"로그아웃 성공\"}");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
            );

        return http.build();
    }

    // ── Chain 2: 나머지 경로 (학생 JWT 방식) ─────────────────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain studentFilterChain(HttpSecurity http, com.skfkfkvlrm.stockgame_spring.auth.JwtFilter jwtFilter) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults()) // 전역 CORS 설정 적용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/members/login", "/members/join", "/members/id-check").permitAll() // 로그인, 회원가입 등 공개
                .requestMatchers("/api/asset/**", "/api/stock/**", "/orders/**", "/coupons/**", "/history").authenticated() // API 보호
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용 (Stateless)
            )
            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            // Spring Security의 기본 로그인 페이지/리다이렉트 비활성화
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
