package com.skfkfkvlrm.stockgame_spring.domain.news;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.news.AiNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news/generate-ai")
@RequiredArgsConstructor
public class AiNewsController {

    private final AiNewsService aiNewsService;

    @PostMapping
    public ApiResponse<String> generateDynamicNews() {
        try {
            aiNewsService.generateDynamicNews();
            return ApiResponse.success("AI 뉴스가 성공적으로 생성되었습니다.", "Success");
        } catch (Exception e) {
            return ApiResponse.error("AI 뉴스 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
