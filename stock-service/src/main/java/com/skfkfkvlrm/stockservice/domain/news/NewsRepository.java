package com.skfkfkvlrm.stockservice.domain.news;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NewsRepository {
    List<NewsResponse> getNewsList();
    void insertNews(String content);
}
