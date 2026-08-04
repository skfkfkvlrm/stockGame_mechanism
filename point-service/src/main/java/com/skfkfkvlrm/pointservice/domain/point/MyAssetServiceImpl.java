package com.skfkfkvlrm.pointservice.domain.point;

import com.skfkfkvlrm.pointservice.domain.common.DashboardResponse;
import com.skfkfkvlrm.pointservice.domain.common.StockInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyAssetServiceImpl implements MyAssetService {
    private final MyAssetRepository myAssetRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String studentId) {
        Integer rawPoint = myAssetRepository.getPointValue(studentId);
        int totalPoint = rawPoint != null ? rawPoint : ("admin".equals(studentId) || "manager".equals(studentId) ? 99999999 : 0);
        
        Integer rawCoupon = myAssetRepository.getTotalCoupon(studentId);
        int totalCoupon = rawCoupon != null ? rawCoupon : 0;

        List<Integer> myStockNos = myAssetRepository.getMyStockNos(studentId, "MATCHED");

        List<StockInfoResponse> stockList = new ArrayList<>();
        int totalStockValue = 0;
        int totalProfit = 0;

        if (myStockNos != null) {
            for (int stockId : myStockNos) {
                int amount = myAssetRepository.getStockAmount(studentId, stockId, "MATCHED");

                if (amount > 0) {
                    String stockName = myAssetRepository.getStockName(stockId);
                    int currentPrice = myAssetRepository.getStockCurrentPrice(stockId);
                    int averagePrice = myAssetRepository.getAveragePrice(studentId, stockId, "MATCHED", "BUY");
                    int purchasePrice = myAssetRepository.getPurchasePrice(studentId, stockId, "MATCHED", "BUY");
                    int profit = myAssetRepository.getStockProfit(studentId, stockId, "MATCHED");

                    totalStockValue += amount * currentPrice;
                    totalProfit += profit;

                    stockList.add(StockInfoResponse.builder()
                            .stockName(stockName)
                            .amount(amount)
                            .currentPrice(currentPrice)
                            .averagePrice(averagePrice)
                            .purchasePrice(purchasePrice)
                            .profit(profit)
                            .build());
                }
            }
        }
        int totalAsset = totalPoint + totalStockValue;

        return DashboardResponse.builder()
                .totalPoint(totalPoint)
                .totalCoupon(totalCoupon)
                .totalAsset(totalAsset)
                .totalProfit(totalProfit)
                .myStocks(stockList)
                .build();
    }
}
