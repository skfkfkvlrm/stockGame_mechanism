package com.skfkfkvlrm.stockservice.domain.stock;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StockDetailRepository {
    Map<String, Object> getStockInfo(@Param("stockId") int stockId);
    Map<String, Object> getStockPubInfo(@Param("stockId") int stockId);
    int getStudentStockAmount(@Param("stockId") int stockId, @Param("studentId") String studentId);
    List<Order> getMatchOrderList(@Param("stockId") int stockId, @Param("content") String content, @Param("price") int price, @Param("studentId") String studentId);
    void insertOrder(Order order);
    void setMatchedOrder(@Param("buyOrderId") Integer buyOrderId, @Param("sellOrderId") Integer sellOrderId, @Param("amount") int amount, @Param("price") int price);
    void setStockPubBalance(@Param("amount") int amount, @Param("stockId") int stockId);
    void setOrderStateMatched(@Param("orderId") int orderId);
    void updateOrderAmount(@Param("amount") int amount, @Param("orderId") int orderId);
    void setOrderStateCancel(@Param("orderId") int orderId);
    StockOrderResponse getOrderById(@Param("orderId") int orderId);
}
