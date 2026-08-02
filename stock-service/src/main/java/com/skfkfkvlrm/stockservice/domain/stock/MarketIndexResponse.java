package com.skfkfkvlrm.stockservice.domain.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketIndexResponse {
    private double kospiIndex;
    private double kospiChangeRate;
    private double kosdaqIndex;
    private double kosdaqChangeRate;
}
