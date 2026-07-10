package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.skfkfkvlrm.stockgame_spring.domain.point.PointHistoryResponse;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyPointHistoryRepository;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class MyPointHistoryController {

    private final MyPointHistoryRepository myPointHistoryRepository;

    @GetMapping({"", "/"})
    public ApiResponse<List<PointHistoryResponse>> getHistory(@org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<PointHistoryResponse> history = myPointHistoryRepository.getMyPointHistoryList(studentId);
        return ApiResponse.success("Point history data", history);
    }
}
