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
        int totalPoint = rawPoint != null ? rawPoint : 0;
        
        Integer rawCoupon = myAssetRepository.getTotalCoupon(studentId);
        int totalCoupon = rawCoupon != null ? rawCoupon : 0;

        List<StockInfoResponse> stockList = myAssetRepository.getMyStockAssetList(studentId);
        if (stockList == null) {
            stockList = new ArrayList<>();
        }

        int totalStockValue = 0;
        int totalProfit = 0;

        for (StockInfoResponse stock : stockList) {
            totalStockValue += stock.getAmount() * stock.getCurrentPrice();
            totalProfit += stock.getProfit();
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
