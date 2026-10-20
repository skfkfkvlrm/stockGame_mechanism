package com.skfkfkvlrm.memberservice.domain.member;

import com.skfkfkvlrm.memberservice.auth.JwtUtil;
import com.skfkfkvlrm.memberservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @PostMapping({"/join", "/join/student"})
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
    @Transactional
    public ApiResponse<Boolean> adjustStudentPoint(
            @PathVariable("studentId") String studentId,
            @RequestBody Map<String, Object> body) {
        int rawAmount = body.get("amount") != null ? ((Number) body.get("amount")).intValue() : 0;
        String type = (String) body.get("type");
        int amount = rawAmount;
        if ("subtract".equalsIgnoreCase(type) || "deduct".equalsIgnoreCase(type)) {
            amount = -Math.abs(rawAmount);
        }

        // 음수 잔액 방어 가드
        Integer currentPoint = memberRepository.getStudentPoint(studentId);
        if (currentPoint == null) {
            return ApiResponse.error("존재하지 않는 학생입니다.");
        }
        if (amount < 0 && (currentPoint + amount) < 0) {
            return ApiResponse.error("차감 후 포인트가 음수가 될 수 없습니다. (현재 잔액: " + currentPoint + " P)");
        }

        String reason = (String) body.getOrDefault("reason", "");
        if (reason == null || reason.isBlank()) {
            reason = amount >= 0 ? "관리자 수동 포인트 지급" : "관리자 수동 포인트 차감";
        }

        memberRepository.updateStudentPoint(amount, studentId);
        memberRepository.insertGetPoint(studentId, amount, reason);
        return ApiResponse.success("학생 포인트가 성공적으로 반영되었습니다.", true);
    }

    @DeleteMapping("/admin/students/{studentId}")
    public ApiResponse<Boolean> deleteStudent(@PathVariable("studentId") String studentId) {
        if ("admin".equalsIgnoreCase(studentId)) {
            return ApiResponse.error("관리자 계정은 삭제할 수 없습니다.");
        }
        int rows = memberRepository.deleteStudent(studentId);
        if (rows > 0) {
            return ApiResponse.success("학생 계정이 성공적으로 삭제되었습니다.", true);
        } else {
            return ApiResponse.error("삭제할 학생을 찾을 수 없습니다.");
        }
    }

    // 데모 원터치 포인트 충전 (+1,000,000 P)
    @PostMapping("/demo/recharge")
    @Transactional
    public ApiResponse<Map<String, Object>> rechargeDemoPoints(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestAttribute(name = "studentId", required = false) String callerStudentId) {
        int amount = 1000000;
        if (body != null && body.get("amount") != null) {
            amount = ((Number) body.get("amount")).intValue();
        }

        String targetId = (callerStudentId != null && !callerStudentId.isBlank()) ? callerStudentId : "demo";
        if (!"demo".equalsIgnoreCase(targetId) && !"recruiter".equalsIgnoreCase(targetId) && !"admin".equalsIgnoreCase(targetId)) {
            return ApiResponse.error("데모 체험 전용 기능입니다. 일반 학생 계정은 사용할 수 없습니다.");
        }

        if (amount <= 0 || amount > 10000000) {
            return ApiResponse.error("1회 충전 가능 금액은 1 P 이상 10,000,000 P 이하여야 합니다.");
        }

        Integer currentPoint = memberRepository.getStudentPoint(targetId);
        if (currentPoint == null) {
            currentPoint = 0;
        }

        if ((long) currentPoint + amount > 100000000L) {
            return ApiResponse.error("데모 계정 최대 보유 한도(100,000,000 P)를 초과할 수 없습니다. (현재 잔액: " + currentPoint + " P)");
        }

        int newPoint = currentPoint + amount;
        memberRepository.updateStudentPoint(amount, targetId);
        memberRepository.insertGetPoint(targetId, amount, "채용담당자 데모 원터치 포인트 충전");

        return ApiResponse.success("데모 포인트가 성공적으로 충전되었습니다.", Map.of(
                "success", true,
                "studentId", targetId,
                "rechargedAmount", amount,
                "totalPoint", newPoint
        ));
    }

    // 학생 데모 본인 계정 초기화 (로그아웃 시 또는 수동 리셋)
    @PostMapping("/demo/reset")
    @Transactional
    public ApiResponse<Boolean> resetDemoAccount(
            @RequestParam(value = "targetStudentId", defaultValue = "demo") String targetStudentId) {
        if (!"demo".equalsIgnoreCase(targetStudentId) && !"recruiter".equalsIgnoreCase(targetStudentId)) {
            return ApiResponse.error("데모 체험 전용 기능입니다. 일반 학생 계정은 초기화할 수 없습니다.");
        }

        memberRepository.resetDemoStudent(targetStudentId);
        memberRepository.clearGetPoints(targetStudentId);
        memberRepository.insertGetPoint(targetStudentId, 1000000, "채용담당자 데모 체험 기초 시드머니 (1,000,000 P)");

        return ApiResponse.success("데모 계정이 초기 상태(1,000,000 P)로 리셋되었습니다.", true);
    }

    // 교실 세션 리셋: 전교생 포인트 및 쿠폰 일괄 초기화
    @PostMapping("/admin/reset-session")
    @Transactional
    public ApiResponse<Boolean> resetClassroomSession(@RequestBody(required = false) Map<String, Object> body) {
        int defaultPoint = 100000;
        if (body != null && body.get("defaultPoint") != null) {
            defaultPoint = ((Number) body.get("defaultPoint")).intValue();
        }

        memberRepository.resetAllStudents(defaultPoint);
        memberRepository.clearAllStudentCoupons();

        return ApiResponse.success("전교생 자산이 성공적으로 초기화되었습니다.", true);
    }

    // 관리자 데모 세션 롤백: 삭제 학생 ACTIVE 복구 및 관리자 포인트 조정분 역산
    @PostMapping("/admin/demo/rollback")
    @Transactional
    public ApiResponse<Map<String, Object>> rollbackAdminDemoSession() {
        int restoredCount = memberRepository.restoreDeletedStudents();

        List<Map<String, Object>> adjustments = memberRepository.getAdminPointAdjustments();
        int reversedAdjustments = 0;
        if (adjustments != null) {
            for (Map<String, Object> adj : adjustments) {
                String studentId = String.valueOf(adj.get("studentId"));
                int net = ((Number) adj.get("netAdjustment")).intValue();
                if (net != 0) {
                    memberRepository.updateStudentPoint(-net, studentId);
                    memberRepository.insertGetPoint(studentId, -net, "[관리자 데모 롤백] 수동 조정분 원상복구");
                    reversedAdjustments++;
                }
            }
        }

        return ApiResponse.success("관리자 데모 세션이 성공적으로 롤백되었습니다.", Map.of(
                "success", true,
                "restoredStudents", restoredCount,
                "reversedPointAdjustments", reversedAdjustments
        ));
    }
}
