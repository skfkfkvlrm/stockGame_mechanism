package com.skfkfkvlrm.stockservice.domain.admin;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "market_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketSettings {
    @Id
    private Integer id;

    @Column(name = "market_open")
    private boolean isMarketOpen;

    @Column(name = "mode")
    @Builder.Default
    private String mode = "AUTO"; // AUTO | MANUAL

    @Column(name = "open_time")
    @Builder.Default
    private String openTime = "09:00";

    @Column(name = "close_time")
    @Builder.Default
    private String closeTime = "15:30";

    @Column(name = "call_auction_start_time")
    @Builder.Default
    private String callAuctionStartTime = "15:20";

    @Column(name = "operating_days")
    @Builder.Default
    private String operatingDays = "MON,TUE,WED,THU,FRI";

    @Column(name = "status_code")
    @Builder.Default
    private String statusCode = "OPEN";

    /**
     * 현재 시각 기준 자동 계산된 개장 여부 및 상태 코드 반환
     */
    public boolean calculateIsMarketOpen() {
        if ("MANUAL".equalsIgnoreCase(this.mode)) {
            return this.isMarketOpen;
        }

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime open = LocalTime.parse(this.openTime != null ? this.openTime : "09:00", formatter);
            LocalTime close = LocalTime.parse(this.closeTime != null ? this.closeTime : "15:30", formatter);
            LocalTime now = LocalTime.now();

            return !now.isBefore(open) && !now.isAfter(close);
        } catch (Exception e) {
            return this.isMarketOpen;
        }
    }

    public String calculateStatusCode() {
        if ("MANUAL".equalsIgnoreCase(this.mode)) {
            return this.isMarketOpen ? "OPEN" : "MANUAL_PAUSE";
        }

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
            return "HOLIDAY";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime open = LocalTime.parse(this.openTime != null ? this.openTime : "09:00", formatter);
            LocalTime auctionStart = LocalTime.parse(this.callAuctionStartTime != null ? this.callAuctionStartTime : "15:20", formatter);
            LocalTime close = LocalTime.parse(this.closeTime != null ? this.closeTime : "15:30", formatter);
            LocalTime now = LocalTime.now();

            if (now.isBefore(open) || now.isAfter(close)) {
                return "CLOSED";
            }
            if (!now.isBefore(auctionStart) && !now.isAfter(close)) {
                return "CALL_AUCTION";
            }
            return "OPEN";
        } catch (Exception e) {
            return this.isMarketOpen ? "OPEN" : "CLOSED";
        }
    }
}
