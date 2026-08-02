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
    private final AppUserRepository appUserRepository;
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
        // 1. students 테이블 조회
        Map<String, Object> savedData = memberRepository.findByStudentId(request.getStudentId());

        if (savedData != null) {
            String savedPassword = (String) savedData.get("password");
            boolean isMatched = false;

            if (savedPassword != null) {
                isMatched = passwordEncoder.matches(request.getPassword(), savedPassword);
            }


            if (isMatched) {
                String role = "admin".equals(request.getStudentId()) ? "ROLE_ADMIN" : "ROLE_STUDENT";
                return StudentResponse.builder()
                        .studentId(request.getStudentId())
                        .name((String) savedData.get("name"))
                        .grade((Integer) savedData.get("grade"))
                        .className((String) savedData.get("class_name"))
                        .totalPoint((Integer) savedData.get("total_point"))
                        .role(role)
                        .build();
            }
        }

        // 2. app_users 테이블 (관리자/매니저 계정) 조회
        var appUserOpt = appUserRepository.findByUsername(request.getStudentId());
        if (appUserOpt.isPresent()) {
            var appUser = appUserOpt.get();
            if (passwordEncoder.matches(request.getPassword(), appUser.getPassword())) {
                return StudentResponse.builder()
                        .studentId(appUser.getUsername())
                        .name(appUser.getRole() == Role.ROLE_ADMIN ? "최고관리자" : "운영매니저")
                        .grade(0)
                        .className("관리자")
                        .classNumber(0)
                        .totalPoint(99999999)
                        .role(appUser.getRole().name())
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

    @Override
    public StudentResponse getMemberInfo(String studentId) {
        Map<String, Object> savedData = memberRepository.findByStudentId(studentId);
        if (savedData != null) {
            return StudentResponse.builder()
                    .studentId(studentId)
                    .name((String) savedData.get("name"))
                    .grade(savedData.get("grade") != null ? ((Number) savedData.get("grade")).intValue() : 0)
                    .className((String) savedData.get("class_name"))
                    .classNumber(savedData.get("class_number") != null ? ((Number) savedData.get("class_number")).intValue() : 0)
                    .totalPoint(savedData.get("total_point") != null ? ((Number) savedData.get("total_point")).intValue() : 0)
                    .role("admin".equals(studentId) ? "ROLE_ADMIN" : "ROLE_STUDENT")
                    .build();
        }

        var appUserOpt = appUserRepository.findByUsername(studentId);
        if (appUserOpt.isPresent()) {
            var appUser = appUserOpt.get();
            return StudentResponse.builder()
                    .studentId(appUser.getUsername())
                    .name(appUser.getRole() == Role.ROLE_ADMIN ? "최고관리자" : "운영매니저")
                    .grade(0)
                    .className("관리자")
                    .classNumber(0)
                    .totalPoint(99999999)
                    .role(appUser.getRole().name())
                    .build();
        }

        throw new com.skfkfkvlrm.stockgame_spring.exception.InvalidCredentialsException();
    }
}
