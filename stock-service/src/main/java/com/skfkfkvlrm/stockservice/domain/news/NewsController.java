package com.skfkfkvlrm.stockservice.domain.news;

import com.skfkfkvlrm.stockservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/news", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class NewsController {

    private final NewsRepository newsRepository;

    @GetMapping
    public ApiResponse<List<NewsResponse>> getNews(@RequestAttribute(name = "studentId", required = false) String studentId) {
        if (studentId == null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null) {
            studentId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (studentId == null || "anonymousUser".equals(studentId)) {
            return ApiResponse.error("로그인이 필요합니다.");
        }
        List<NewsResponse> newsList = newsRepository.getNewsList();
        return ApiResponse.success("News data", newsList);
    }
}
