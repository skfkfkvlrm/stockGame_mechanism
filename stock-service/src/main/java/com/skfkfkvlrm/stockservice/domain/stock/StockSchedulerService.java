package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockSchedulerService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StockSchedulerService.class);

    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    /**
     * 留ㅼ???(00:00:00)? ?ㅽ????댁?쇱 醫媛瑜?stocks ??대?? prev_price 濡 諛??⑸??
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void syncPreviousDayPrices() {
        log.info("Starting daily sync for prev_price...");
        try {
            stockPriceHistoryRepository.updatePrevPricesToLatestClose();
            log.info("Successfully updated prev_price for all stocks to the latest close price.");
        } catch (Exception e) {
            log.error("Failed to update prev_price", e);
        }
    }
}
