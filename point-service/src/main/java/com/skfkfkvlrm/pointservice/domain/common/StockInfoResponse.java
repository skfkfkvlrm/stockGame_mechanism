package com.skfkfkvlrm.pointservice.domain.common;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockInfoResponse {
    private String stockName;
    private int amount;
    private int currentPrice;
    private int averagePrice;
    private int purchasePrice;
    private int profit;
}
