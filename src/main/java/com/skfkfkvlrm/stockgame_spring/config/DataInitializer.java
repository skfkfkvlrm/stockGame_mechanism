package com.skfkfkvlrm.stockgame_spring.config;

import com.skfkfkvlrm.stockgame_spring.domain.member.AppUser;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockgame_spring.domain.member.Role;
import com.skfkfkvlrm.stockgame_spring.domain.member.AppUserRepository;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 초기 관리자 계정을 자동 생성합니다.
 * 이미 존재하는 계정은 덮어쓰지 않습니다.
 *
 * 운영 환경에서는 초기 비밀번호를 application.yaml 환경변수로 분리해야 합니다.
 * 예: ${ADMIN_PASSWORD:admin1234}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final MarketSettingsRepository marketSettingsRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createDefaultAdminIfAbsent();
        createDefaultManagerIfAbsent();
        createDefaultMarketSettingsIfAbsent();
        createDummyDataIfEmpty();
    }

    private void createDefaultAdminIfAbsent() {
        if (!appUserRepository.existsByUsername("admin")) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin1234")) // 운영 시 환경변수로 교체
                    .role(Role.ROLE_ADMIN)
                    .build();
            appUserRepository.save(admin);
            log.info("[DataInitializer] ADMIN 초기 계정 생성 완료 (username: admin)");
        } else {
            log.debug("[DataInitializer] ADMIN 계정이 이미 존재합니다.");
        }
    }

    private void createDefaultManagerIfAbsent() {
        if (!appUserRepository.existsByUsername("manager")) {
            AppUser manager = AppUser.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager1234")) // 운영 시 환경변수로 교체
                    .role(Role.ROLE_MANAGER)
                    .build();
            appUserRepository.save(manager);
            log.info("[DataInitializer] MANAGER 초기 계정 생성 완료 (username: manager)");
        } else {
            log.debug("[DataInitializer] MANAGER 계정이 이미 존재합니다.");
        }
    }

    private void createDefaultMarketSettingsIfAbsent() {
        if (!marketSettingsRepository.existsById(1)) {
            MarketSettings settings = MarketSettings.builder()
                    .id(1)
                    .marketOpen(true) // 기본적으로 개장 상태
                    .dailyTradeLimit(0) // 0은 무제한
                    .build();
            marketSettingsRepository.save(settings);
            log.info("[DataInitializer] MarketSettings 기본 설정 생성 완료 (marketOpen: true)");
        }
    }

    private void createDummyDataIfEmpty() {
        try {
            log.info("[DataInitializer] 1차 스프린트 더미 데이터를 삽입합니다 (강제 실행)...");

            try {
                jdbcTemplate.execute("ALTER TABLE coupons DROP COLUMN student_id");
            } catch (Exception e) {
                // Ignore if column does not exist
            }

            // Clean up duplicates if any
            try {
                jdbcTemplate.update("DELETE FROM stocks WHERE stock_id > 3");
                jdbcTemplate.update("DELETE FROM coupons WHERE coupon_id > 4");
                jdbcTemplate.update("DELETE FROM news WHERE news_id > 5");
                jdbcTemplate.update("DELETE FROM students WHERE id > 3");
                jdbcTemplate.update("DELETE FROM app_users WHERE id > 3");
            } catch (Exception e) {
                log.warn("Cleanup error: " + e.getMessage());
            }

            // Students
            Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
            if (studentCount != null && studentCount == 0) {
                jdbcTemplate.update(
                        "INSERT IGNORE INTO students (student_id, password, name, grade, class_name, class_number, register_year, total_coupon, total_point, created_date) VALUES "
                                +
                                "('abc', 'abc123!', '홍길동', 5, '4', 63, 2026, 0, 30000, NOW()), " +
                                "('def', 'def123!', '이순신', 5, '4', 9, 2026, 0, 30000, NOW()), " +
                                "('dldlsghk123', 'dldlsghk123!', '김철수', 6, '가', 63, 2026, 0, 8000, NOW())");
            }

            // Stocks
            Integer stockCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stocks", Integer.class);
            if (stockCount != null && stockCount == 0) {
                jdbcTemplate.update(
                        "INSERT IGNORE INTO stocks (name, content, publication_balance, publication_price, prev_price, created_date) VALUES "
                                +
                                "('새콤달콤', '화가나고 피곤할 땐 새콤달콤', 10, 800, 800, NOW()), " +
                                "('PC방', '친구들과 함께 발로란트 한 판?', 100, 2000, 2000, NOW()), " +
                                "('SM', '에스파 짱', 100, 4000, 4000, NOW())");
            }

            // Coupons
            Integer couponCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupons", Integer.class);
            if (couponCount != null && couponCount == 0) {
                jdbcTemplate.update("INSERT INTO coupons (name, price, created_date) VALUES " +
                        "('자리 교환권', 500, NOW()), " +
                        "('청소당번 면제', 3000, NOW()), " +
                        "('자리 뺏기', 100000, NOW()), " +
                        "('안마 쿠폰', 1000, NOW())");
            }

            // News
            Integer newsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news", Integer.class);
            if (newsCount != null && newsCount == 0) {
                jdbcTemplate.update("INSERT INTO news (content, created_date) VALUES " +
                        "('새콤달콤 희귀템 추가 입고 예정입니다.', NOW()), " +
                        "('에스파 슈퍼노바로 컴백 무대, 화제 만발입니다.', NOW()), " +
                        "('청소당번 면제권, 품귀 현상으로 가격 폭등 중입니다.', NOW()), " +
                        "('SM 신인 아티스트 데뷔 예고, 기대감이 고조되고 있습니다.', NOW()), " +
                        "('최신 게임 출시 임박에 PC 방, 만석 행렬입니다.', NOW())");
            }

            log.info("[DataInitializer] 더미 데이터 삽입 완료!");
        } catch (Exception e) {
            log.warn("[DataInitializer] 더미 데이터 삽입 중 오류 발생 (테이블이 없거나 매핑 오류일 수 있습니다): {}", e.getMessage());
        }
    }
}
