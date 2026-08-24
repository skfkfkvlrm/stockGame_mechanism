package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockservice.domain.admin.MarketSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MarketScheduler {

    private final MarketSettingsRepository marketSettingsRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private String lastStatusCode = "";
    private Boolean lastIsOpen = null;

    /**
     * 10초 주기로 현재 시장 개폐 상태를 자동 평가하고, 상태 변경 시 WebSocket 브로드캐스트
     */
    @Scheduled(fixedRate = 10000, initialDelay = 3000)
    public void evaluateMarketStatus() {
        try {
            MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
            if (settings == null) {
                return;
            }

            boolean currentIsOpen = settings.calculateIsMarketOpen();
            String currentStatus = settings.calculateStatusCode();

            // AUTO 모드인 경우 주기적으로 DB의 isMarketOpen과 statusCode를 최신 계산값으로 동기화
            if ("AUTO".equalsIgnoreCase(settings.getMode())) {
                if (settings.isMarketOpen() != currentIsOpen || !currentStatus.equals(settings.getStatusCode())) {
                    settings.setMarketOpen(currentIsOpen);
                    settings.setStatusCode(currentStatus);
                    marketSettingsRepository.save(settings);
                    log.info("[MarketScheduler] 시장 상태 자동 전환: mode={}, isOpen={}, status={}", settings.getMode(), currentIsOpen, currentStatus);
                }
            }

            // 상태가 이전과 달라졌다면 WebSocket으로 모든 클라이언트에 브로드캐스트
            if (lastIsOpen == null || lastIsOpen != currentIsOpen || !currentStatus.equals(lastStatusCode)) {
                lastIsOpen = currentIsOpen;
                lastStatusCode = currentStatus;

                Map<String, Object> payload = new HashMap<>();
                payload.put("marketOpen", currentIsOpen);
                payload.put("mode", settings.getMode());
                payload.put("openTime", settings.getOpenTime());
                payload.put("closeTime", settings.getCloseTime());
                payload.put("statusCode", currentStatus);

                messagingTemplate.convertAndSend("/topic/market/status", payload);
                log.info("[MarketScheduler] WebSocket 시장 상태 브로드캐스트: {}", payload);
            }
        } catch (Exception e) {
            log.error("[MarketScheduler] 시장 상태 평가 중 예외 발생: {}", e.getMessage());
        }
    }
}
