package com.skfkfkvlrm.stockgame_spring.domain.member;

public enum Role {
    ROLE_USER,      // 학생 (기존 HttpSession 방식)
    ROLE_MANAGER,   // 운영 관리자 (/admin/** 접근)
    ROLE_ADMIN      // 최고 관리자 (모든 /admin/** 접근)
}
