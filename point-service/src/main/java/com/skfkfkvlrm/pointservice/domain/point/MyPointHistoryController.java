package com.skfkfkvlrm.pointservice.domain.point;

import com.skfkfkvlrm.pointservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class MyPointHistoryController {

    private final MyPointHistoryRepository myPointHistoryRepository;

    @GetMapping({"", "/"})
    public ApiResponse<List<PointHistoryResponse>> getHistory(@RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<PointHistoryResponse> history = myPointHistoryRepository.getMyPointHistoryList(studentId);
        return ApiResponse.success("Point history data", history);
    }

    @GetMapping("/admin/{studentId}")
    public ApiResponse<List<PointHistoryResponse>> getStudentPointHistoryAdmin(@org.springframework.web.bind.annotation.PathVariable("studentId") String studentId) {
        List<PointHistoryResponse> history = myPointHistoryRepository.getMyPointHistoryList(studentId);
        return ApiResponse.success("Student point history data fetched", history);
    }
}
