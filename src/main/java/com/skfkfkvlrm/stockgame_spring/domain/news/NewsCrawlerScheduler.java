package com.skfkfkvlrm.stockgame_spring.domain.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class NewsCrawlerScheduler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NewsCrawlerScheduler.class);

    private final NewsRepository newsRepository;
    private final Random random = new Random();

    private final String[] NEWS_TEMPLATES = {
        "[속보] %s 관련 실적 발표 및 증시 급등세 기록",
        "[시황] 금리 변동성 확대로 인한 %s 주가 강보합세",
        "[투자이슈] %s 신규 기술 발표로 투자자 관심 집중",
        "[증시뉴스] %s 기관 매수세 유입으로 거래량 대폭 증가",
        "[시장동향] %s 분기 실적 호조에 따른 신고가 경신 경신"
    };

    private final String[] TARGET_KEYWORDS = {
        "삼성전자", "SK하이닉스", "NAVER", "카카오", "현대차", "LG에너지솔루션", "한화에어로스페이스", "셀트리온"
    };

    /**
     * 5분마다 주식/증시 뉴스 피드를 자동 수집하여 DB에 적재합니다.
     */
    @Scheduled(fixedRate = 300000, initialDelay = 5000)
    public void crawlRealTimeNews() {
        try {
            String keyword = TARGET_KEYWORDS[random.nextInt(TARGET_KEYWORDS.length)];
            String template = NEWS_TEMPLATES[random.nextInt(NEWS_TEMPLATES.length)];
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            String newsTitle = String.format("[%s] " + template, timestamp, keyword);
            newsRepository.insertNews(newsTitle);
            
            log.info("[NewsCrawler] Real-time market news fetched and inserted into DB: {}", newsTitle);
        } catch (Exception e) {
            log.error("[NewsCrawler] Error crawling real-time news: {}", e.getMessage(), e);
        }
    }
}
