package com.skfkfkvlrm.stockgame_spring.domain.news;

import com.skfkfkvlrm.stockgame_spring.domain.common.ApiResponse;
import com.skfkfkvlrm.stockgame_spring.domain.news.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsRepository newsRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping
    public ApiResponse<List<String>> getNews(@org.springframework.web.bind.annotation.RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<String> newsList = newsRepository.getNewsList();
        return ApiResponse.success("News data", newsList);
    }
    @GetMapping("/db-check")
    public List<java.util.Map<String, Object>> dbCheck() {
        return jdbcTemplate.queryForList("DESCRIBE coupons");
    }
}
