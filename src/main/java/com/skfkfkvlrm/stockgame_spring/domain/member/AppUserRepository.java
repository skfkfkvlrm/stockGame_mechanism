package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.skfkfkvlrm.stockgame_spring.domain.member.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** 로그인 처리 시 username으로 계정 조회 */
    Optional<AppUser> findByUsername(String username);

    /** DataInitializer에서 중복 계정 생성 방지 */
    boolean existsByUsername(String username);
}
