package com.skfkfkvlrm.pointservice.domain.point;

import com.skfkfkvlrm.pointservice.domain.common.ApiResponse;
import com.skfkfkvlrm.pointservice.domain.common.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class MyAssetController {
    private final MyAssetService myAssetService;

    @GetMapping({"", "/"})
    public ApiResponse<DashboardResponse> getDashboard(@RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        DashboardResponse dashboardData = myAssetService.getDashboard(studentId);
        return ApiResponse.success("자산 대시보드 조회 성공", dashboardData);
    }

    @GetMapping("/admin/students/{targetStudentId}/detail")
    public ApiResponse<DashboardResponse> getStudentDetailDashboard(@org.springframework.web.bind.annotation.PathVariable("targetStudentId") String targetStudentId) {
        DashboardResponse dashboardData = myAssetService.getDashboard(targetStudentId);
        return ApiResponse.success("학생 자산 대시보드 조회 성공", dashboardData);
    }
}
