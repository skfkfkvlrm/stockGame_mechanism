package com.skfkfkvlrm.stockgame_spring.domain.member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 관리자/매니저 계정 엔티티.
 * 기존 Student(학생) 엔티티와 완전히 별개 테이블(app_users)로 분리.
 * Spring Security UserDetailsService에서만 사용됨.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인 ID (중복 불가) */
    @Column(unique = true, nullable = false)
    private String username;

    /** BCrypt 해싱된 비밀번호 */
    @Column(nullable = false)
    private String password;

    /** 역할: ROLE_MANAGER 또는 ROLE_ADMIN */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdDate;
}
