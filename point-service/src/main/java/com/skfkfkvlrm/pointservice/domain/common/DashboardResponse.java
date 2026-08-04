package com.skfkfkvlrm.pointservice.domain.common;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private int totalAsset;
    private int totalPoint;
    private int totalCoupon;
    private int totalProfit;
    private List<StockInfoResponse> myStocks;
}
