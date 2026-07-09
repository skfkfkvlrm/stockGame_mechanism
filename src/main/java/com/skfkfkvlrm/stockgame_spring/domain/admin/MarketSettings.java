package com.skfkfkvlrm.stockgame_spring.domain.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private boolean marketOpen;

    private int dailyTradeLimit;
}
