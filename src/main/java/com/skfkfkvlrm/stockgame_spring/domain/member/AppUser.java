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

    public AppUser() {}

    public AppUser(Long id, String username, String password, Role role, LocalDateTime createdDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdDate = createdDate;
    }

    public static AppUserBuilder builder() {
        return new AppUserBuilder();
    }

    public static class AppUserBuilder {
        private Long id;
        private String username;
        private String password;
        private Role role;
        private LocalDateTime createdDate;

        public AppUserBuilder id(Long id) { this.id = id; return this; }
        public AppUserBuilder username(String username) { this.username = username; return this; }
        public AppUserBuilder password(String password) { this.password = password; return this; }
        public AppUserBuilder role(Role role) { this.role = role; return this; }
        public AppUserBuilder createdDate(LocalDateTime createdDate) { this.createdDate = createdDate; return this; }

        public AppUser build() {
            return new AppUser(id, username, password, role, createdDate);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
