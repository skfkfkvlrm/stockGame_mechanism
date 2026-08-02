package com.skfkfkvlrm.memberservice.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        int member = memberRepository.setMember(request);
        return member > 0;
    }

    @Override
    @Transactional
    public StudentResponse login(StudentLoginRequest request) {
        System.out.println("[MemberService Debug] Login Attempt - studentId: " + request.getStudentId() + ", passLen: " + (request.getPassword() != null ? request.getPassword().length() : 0));
        // 1. students 테이블 조회
        Map<String, Object> savedData = memberRepository.findByStudentId(request.getStudentId());
        System.out.println("[MemberService Debug] savedData found: " + (savedData != null));


        if (savedData != null) {
            String savedPassword = (String) savedData.get("password");
            boolean isMatched = false;

            if (savedPassword != null) {
                if (savedPassword.startsWith("$2a$") || savedPassword.startsWith("$2b$")) {
                    isMatched = passwordEncoder.matches(request.getPassword(), savedPassword);
                } else {
                    isMatched = request.getPassword().equals(savedPassword);
                }
            }

            if (isMatched) {
                String role = "admin".equals(request.getStudentId()) ? "ROLE_ADMIN" : "ROLE_STUDENT";
                return StudentResponse.builder()
                        .studentId(request.getStudentId())
                        .name((String) savedData.get("name"))
                        .grade(savedData.get("grade") != null ? ((Number) savedData.get("grade")).intValue() : 0)
                        .className((String) savedData.get("class_name"))
                        .classNumber(savedData.get("class_number") != null ? ((Number) savedData.get("class_number")).intValue() : 0)
                        .totalPoint(savedData.get("total_point") != null ? ((Number) savedData.get("total_point")).intValue() : 0)
                        .role(role)
                        .build();
            } else {
                System.out.println("[MemberService Debug] Password mismatch for studentId: " + request.getStudentId() + ". Saved: " + savedPassword + " vs Input: " + request.getPassword());
            }

        }


        // 2. app_users 테이블 조회
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

        // 3. 디폴트 admin fallback (admin / 1234)
        if ("admin".equals(request.getStudentId()) && "1234".equals(request.getPassword())) {
            return StudentResponse.builder()
                    .studentId("admin")
                    .name("최고관리자")
                    .grade(0)
                    .className("관리자")
                    .classNumber(0)
                    .totalPoint(99999999)
                    .role("ROLE_ADMIN")
                    .build();
        }

        throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
    }


    @Override
    public boolean getIdCheck(String studentId) {
        return memberRepository.getIdCheck(studentId) > 0;
    }

    @Override
    public List<StudentRankingResponse> getStudentRanking() {
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

        if ("admin".equals(studentId)) {
            return StudentResponse.builder()
                    .studentId("admin")
                    .name("최고관리자")
                    .grade(0)
                    .className("관리자")
                    .classNumber(0)
                    .totalPoint(99999999)
                    .role("ROLE_ADMIN")
                    .build();
        }

        throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");

    }
}
