package com.skfkfkvlrm.stockservice.domain.admin;

import jakarta.persistence.*;
import lombok.*;

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
}
