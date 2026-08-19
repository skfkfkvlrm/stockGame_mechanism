package com.skfkfkvlrm.adminservice.domain.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
