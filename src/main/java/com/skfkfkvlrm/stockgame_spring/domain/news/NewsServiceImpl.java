package com.skfkfkvlrm.stockgame_spring.domain.news;

import com.skfkfkvlrm.stockgame_spring.domain.news.NewsRepository;
import com.skfkfkvlrm.stockgame_spring.domain.news.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;

    @Override
    public List<String> getNewsList() {
        List<String> newsList = newsRepository.getNewsList();

        return newsList;
    }
}
