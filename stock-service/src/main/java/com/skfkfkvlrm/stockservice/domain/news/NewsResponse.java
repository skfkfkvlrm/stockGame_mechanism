package com.skfkfkvlrm.stockservice.domain.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    private int newsId;
    private String content;
    private LocalDateTime createdDate;
}
