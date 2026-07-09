package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.skfkfkvlrm.stockgame_spring.domain.stock.OrderStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyAssetRepository {

    Integer getMyValue(int stockId, String studentId);

    Integer getPointValue(String studentId);

    Integer getTotalProfit(int stockId, String studentId, OrderStatus state);

    Integer getTotalCoupon(String studentId);

    String getStockName(int stockId);

    Integer getStockAmount(String studentId, int stockId, OrderStatus state);

    Integer getAveragePrice(String studentId, int stockId, OrderStatus state, String content);

    Integer getPurchasePrice(String studentId, int stockId, OrderStatus state, String content);

    Integer getStockProfit(String studentId, int stockId, OrderStatus state);

    List<Integer> getMyStockNos(String studentId, OrderStatus state);
}
