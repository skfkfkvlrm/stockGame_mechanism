package com.skfkfkvlrm.stockgame_spring.auth;

import com.skfkfkvlrm.stockgame_spring.domain.member.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security의 UserDetails 구현체.
 * AppUser(관리자/매니저) 엔티티를 Security 인증 객체로 래핑함.
 */
public class AppUserDetails implements UserDetails {

    private final AppUser appUser;

    public AppUserDetails(AppUser appUser) {
        this.appUser = appUser;
    }

    /** 권한 목록: Role enum 이름 그대로 사용 (ROLE_ADMIN, ROLE_MANAGER) */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(appUser.getRole().name()));
    }

    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return appUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /** 원본 엔티티 접근이 필요한 경우 사용 */
    public AppUser getAppUser() {
        return appUser;
    }
}
