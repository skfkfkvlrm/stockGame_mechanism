package com.skfkfkvlrm.memberservice.domain.member;

import com.skfkfkvlrm.memberservice.auth.JwtUtil;
import com.skfkfkvlrm.memberservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/join")
    public ApiResponse<Boolean> join(@RequestBody StudentJoinRequest request) {
        boolean isJoined = memberService.join(request);
        return ApiResponse.success("회원가입 성공", isJoined);
    }

    @PostMapping("/login")
    public ApiResponse<StudentResponse> login(@RequestBody StudentLoginRequest request) {
        try {
            StudentResponse response = memberService.login(request);
            String token = jwtUtil.createToken(response.getStudentId(), response.getRole());
            response.setToken(token);
            return ApiResponse.success("로그인 성공", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("로그인 중 오류가 발생했습니다: " + e.getMessage());
        }
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
    public ApiResponse<StudentResponse> getMe(@RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        StudentResponse response = memberService.getMemberInfo(studentId);
        return ApiResponse.success("내 정보 조회 성공", response);
    }

    @GetMapping("/ranking")
    public ApiResponse<List<StudentRankingResponse>> getStudentRanking() {
        List<StudentRankingResponse> rankingList = memberService.getStudentRanking();
        return ApiResponse.success("학생 랭킹 조회 성공", rankingList);
    }

    @PostMapping("/admin/students/{studentId}/point")
    public ApiResponse<Boolean> adjustStudentPoint(
            @PathVariable("studentId") String studentId,
            @RequestBody java.util.Map<String, Object> body) {
        int amount = body.get("amount") != null ? ((Number) body.get("amount")).intValue() : 0;
        memberRepository.updateStudentPoint(amount, studentId);
        return ApiResponse.success("학생 포인트가 성공적으로 반영되었습니다.", true);
    }

    @DeleteMapping("/admin/students/{studentId}")
    public ApiResponse<Boolean> deleteStudent(@PathVariable("studentId") String studentId) {
        int rows = memberRepository.deleteStudent(studentId);
        if (rows > 0) {
            return ApiResponse.success("학생 계정이 성공적으로 삭제되었습니다.", true);
        } else {
            return ApiResponse.error("삭제할 학생을 찾을 수 없습니다.");
        }
    }
}
