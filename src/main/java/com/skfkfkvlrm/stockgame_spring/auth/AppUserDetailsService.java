package com.skfkfkvlrm.stockgame_spring.auth;

import com.skfkfkvlrm.stockgame_spring.domain.member.AppUser;
import com.skfkfkvlrm.stockgame_spring.domain.member.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security가 로그인 시 호출하는 UserDetailsService 구현체.
 * admin FilterChain (Chain 1)에서만 사용됨.
 * 학생 인증은 기존 MemberServiceImpl + HttpSession 방식을 그대로 사용.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "관리자 계정을 찾을 수 없습니다: " + username));
        return new AppUserDetails(user);
    }
}
