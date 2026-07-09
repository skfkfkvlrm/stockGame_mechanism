package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.skfkfkvlrm.stockgame_spring.domain.common.DashboardResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockInfoResponse;
import com.skfkfkvlrm.stockgame_spring.domain.stock.OrderStatus;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyAssetRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyAssetService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyAssetServiceImpl implements MyAssetService {
    private final MyAssetRepository myAssetRepository;
    private final StockDetailRepository stockDetailRepository;



    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String studentId) {
        int totalPoint = myAssetRepository.getPointValue(studentId);
        int totalCoupon = myAssetRepository.getTotalCoupon(studentId);

        List<Integer> myStockNos = myAssetRepository.getMyStockNos(studentId, OrderStatus.체결);

        List<StockInfoResponse> stockList = new ArrayList<>();
        int totalStockValue = 0;
        int totalProfit = 0;

        for (int stockId : myStockNos) {
            int amount = myAssetRepository.getStockAmount(studentId, stockId, OrderStatus.체결);

            if (amount > 0) {
                String stockName = myAssetRepository.getStockName(stockId);
                int currentPrice = stockDetailRepository.getStockPrice(stockId);
                int averagePrice = myAssetRepository.getAveragePrice(studentId, stockId, OrderStatus.체결, "매수");
                int purchasePrice = myAssetRepository.getPurchasePrice(studentId, stockId, OrderStatus.체결, "매수");
                int profit = myAssetRepository.getStockProfit(studentId, stockId, OrderStatus.체결);

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
