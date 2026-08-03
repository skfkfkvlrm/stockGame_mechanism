package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketIndexResponse {
    private String name;
    private double value;
    private double change;
    private double changeRate;
}
