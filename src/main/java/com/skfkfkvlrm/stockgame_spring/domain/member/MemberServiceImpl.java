package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.skfkfkvlrm.stockgame_spring.domain.member.StudentJoinRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentLoginRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentResponse;
import com.skfkfkvlrm.stockgame_spring.domain.member.MemberRepository;
import com.skfkfkvlrm.stockgame_spring.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean join(StudentJoinRequest request) {
        // 비밀번호 BCrypt 해싱
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        int member = memberRepository.setMember(request);
        return member > 0;
    }

    @Override
    @Transactional
    public StudentResponse login(StudentLoginRequest request) {
        Map<String, Object> savedData = memberRepository.findByStudentId(request.getStudentId());

        if (savedData != null) {
            String savedPassword = (String) savedData.get("password");
            boolean isMatched = false;

            if (savedPassword != null && savedPassword.startsWith("$2a$")) {
                // BCrypt 해시인 경우
                isMatched = passwordEncoder.matches(request.getPassword(), savedPassword);
            } else if (savedPassword != null && savedPassword.equals(request.getPassword())) {
                // 평문 비밀번호인 경우 (마이그레이션)
                isMatched = true;
                // 평문 비밀번호를 BCrypt로 인코딩하여 DB 업데이트
                memberRepository.updatePassword(request.getStudentId(), passwordEncoder.encode(request.getPassword()));
            }

            if (isMatched) {
                return StudentResponse.builder()
                        .studentId(request.getStudentId())
                        .name((String) savedData.get("name"))
                        .grade((Integer) savedData.get("grade"))
                        .className((String) savedData.get("class_name"))
                        .totalPoint((Integer) savedData.get("total_point"))
                        .build();
            }
        }
        throw new com.skfkfkvlrm.stockgame_spring.exception.InvalidCredentialsException();
    }

    @Override
    public boolean getIdCheck(String studentId) {
        return memberRepository.getIdCheck(studentId) > 0;
    }

    @Override
    public java.util.List<StudentRankingResponse> getStudentRanking() {
        return memberRepository.getStudentRanking();
    }
}
