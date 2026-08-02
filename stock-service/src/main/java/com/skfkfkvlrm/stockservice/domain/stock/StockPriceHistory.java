package com.skfkfkvlrm.stockservice.domain.stock;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_price_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id")
    private int stockId;

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "close_price")
    private int closePrice;

    private int volume;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
