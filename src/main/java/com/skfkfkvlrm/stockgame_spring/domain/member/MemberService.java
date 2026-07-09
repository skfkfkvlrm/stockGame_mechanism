package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.skfkfkvlrm.stockgame_spring.domain.member.StudentJoinRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentLoginRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentResponse;

public interface MemberService {
    // 회원가입
    boolean join(StudentJoinRequest request);
    // 로그인
    StudentResponse login(StudentLoginRequest request);
    // 아이디 중복체크
    boolean getIdCheck(String studentId);
}
