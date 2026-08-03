package com.skfkfkvlrm.stockservice.domain.stock;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "stock_price_history",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stockId", "baseDate"})
    }
)
public class StockPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(nullable = false)
    private int stockId;

    @Column(nullable = false)
    private LocalDate baseDate;

    @Column(nullable = false)
    private int openPrice;

    @Column(nullable = false)
    private int highPrice;

    @Column(nullable = false)
    private int lowPrice;

    @Column(nullable = false)
    private int closePrice;

    @Column(nullable = false)
    private int volume;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
