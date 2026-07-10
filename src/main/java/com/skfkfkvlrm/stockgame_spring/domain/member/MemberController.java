package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.skfkfkvlrm.stockgame_spring.domain.member.StudentJoinRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentLoginRequest;
import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentResponse;
import com.skfkfkvlrm.stockgame_spring.domain.member.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final com.skfkfkvlrm.stockgame_spring.auth.JwtUtil jwtUtil;

    @PostMapping("/join")
    public ApiResponse<Boolean> join(@RequestBody StudentJoinRequest request) {
        boolean isJoined = memberService.join(request);
        return ApiResponse.success("회원가입 성공", isJoined);
    }

    @PostMapping("/login")
    public ApiResponse<StudentResponse> login(@RequestBody StudentLoginRequest request) {
        StudentResponse response = memberService.login(request);
        String token = jwtUtil.createToken(response.getStudentId());
        // Return token in response header or body (putting in token field if exists, but StudentResponse might not have it. Let's just return a map or set a header)
        return ApiResponse.success(token, response); // Using message field for token temporarily to avoid changing DTO
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success("로그아웃 성공", true);
    }

    @GetMapping("/id-check")
    public ApiResponse<Boolean> idCheck(@RequestParam("studentId") String studentId) {
        boolean isDuplicate = memberService.getIdCheck(studentId);
        return ApiResponse.success(isDuplicate ? "이미 사용중인 아이디입니다." : "사용 가능한 아이디입니다.", isDuplicate);
    }

    @GetMapping("/me")
    public ApiResponse<StudentResponse> getMe(@org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        
        // Return minimal for now, or fetch from DB if needed
        return ApiResponse.success("내 정보 조회 성공", StudentResponse.builder().studentId(studentId).build());
    }
}