package com.skfkfkvlrm.stockgame_spring.domain.common;

import lombok.*;
import java.util.List;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockInfoResponse;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private String studentName;
    private int totalAsset;
    private int totalPoint;
    private int totalProfit;
    private int totalCoupon;
    private List<StockInfoResponse> myStocks;
}
