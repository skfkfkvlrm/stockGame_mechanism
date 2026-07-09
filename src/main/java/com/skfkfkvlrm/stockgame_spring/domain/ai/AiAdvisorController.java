package com.skfkfkvlrm.stockgame_spring.domain.ai;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.ai.AiAdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;

    @GetMapping("/advisor")
    public ApiResponse<String> getAdvisorMessage(@RequestParam("stockId") int stockId) {
        try {
            String message = aiAdvisorService.getAdvisorMessage(stockId);
            return ApiResponse.success("조언 생성 성공", message);
        } catch (Exception e) {
            return ApiResponse.error("조언 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
