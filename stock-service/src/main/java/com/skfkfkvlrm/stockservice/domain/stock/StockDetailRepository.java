package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockOrderResponse;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import com.skfkfkvlrm.stockservice.domain.stock.OrderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockDetailRepository {
    String setSellOrder(String studentId, int sellPrice, int sellAmount, int stockId);
    String setBuyOrder(String studentId, int buyPrice, int buyAmount, int stockId);

    Map<String, Object> getStockInfo(int stockId);
    int getStockPrice(int stockId);
    int getStockPriceChange(int stockId);
    double getChangeRate(int stockId);
    int getPervPrice(int stockId);
    int getTradeVolume(int stockId);

    Integer getStudentPoint(String studentId);
    int getStudentStockAmount(@Param("stockId") int stockId, @Param("studentId") String studentId);
    boolean setStudentPointDown(@Param("totalPrice") int totalPrice, @Param("studentId") String studentId);
    boolean setStudentPointUp(@Param("totalPrice") int totalPrice, @Param("studentId") String studentId);

    boolean setOrderRequest(OrderStatus content, int price, int amount, OrderStatus state, String studentId, int stockId);
    List<Order> getTotalOrder(int stockId);
    List<Order> getTotalSellOrder(int stockId);
    List<Order> getTotalBuyOrder(int stockId);
    List<Order> getTotalMyOrder(@Param("stockId") int stockId, @Param("studentId") String studentId);
    int getMyOrderNo(OrderStatus content, String studentId, int stockId, OrderStatus state, int amount, int price);

    Map<String, Object> getStockPubInfo(int stockId);
    boolean setStockPubBalance(@Param("buyAmount") int buyAmount, @Param("stockId") int stockId);

    boolean setMatchedOrder(@Param("buyOrderId") int buyOrderId, 
                            @Param("sellOrderId") Integer sellOrderId,
                            @Param("amount") int amount,
                            @Param("price") int price);

    boolean setOrderStatePending(int orderId);
    boolean setOrderStateMatched(int orderId);
    boolean setOrderStateCancel(int orderId);

    Map<String, Object> getMatchOrder(int stockId, int orderPrice, int orderAmount, String studentId, OrderStatus content);

    List<Order> getMatchOrderList(@Param("stockId") int stockId,
                                  @Param("content") String content,
                                  @Param("orderPrice") int orderPrice,
                                  @Param("studentId") String studentId);

    void updateOrder(StockOrderResponse response);
    boolean updateOrderAmount(@Param("amount") int amount, @Param("orderId") int orderId);

    StockOrderResponse getOrderById(int orderId);
    int insertOrder(Order order);
    int cancelWaitingOrdersByStockId(@Param("stockId") int stockId);
    List<Order> getWaitingOrdersByStockId(@Param("stockId") int stockId);
    List<Map<String, Object>> getHoldingsByStockId(@Param("stockId") int stockId);
    List<Map<String, Object>> getStockTransactionsByStockId(@Param("stockId") int stockId);
    int insertGetPoint(@Param("studentId") String studentId, @Param("point") int point, @Param("content") String content);
}