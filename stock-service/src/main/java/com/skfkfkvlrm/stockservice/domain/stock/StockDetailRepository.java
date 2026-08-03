package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockOrderResponse;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import com.skfkfkvlrm.stockservice.domain.stock.OrderStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockDetailRepository {
    // 留ㅻ 二쇰Ц?泥
    String setSellOrder(String studentId, int sellPrice,  int sellAmount, int stockId);

    // 留ㅼ 二쇰Ц?泥
    String setBuyOrder(String studentId, int buyPrice, int buyAmount, int stockId);

    // 二쇱 湲곕낯?蹂?議고
    Map<String, Object> getStockInfo(int stockId);

    // 二쇱 ???媛寃?議고
    int getStockPrice(int stockId);

    // 二쇱 ?댁 媛寃??鍮 議고
    int getStockPriceChange(int stockId);

    // 二쇱 ?깅쎈? 議고
    double getChangeRate(int stockId);

    // 二쇱 ?댁媛寃?議고
    int getPervPrice(int stockId);

    // ??? 媛??蹂댁 ?ъ명?議고
    int getStudentPoint(String studentId);

    // ??? ?뱀 二쇱 蹂댁?? 議고
    int getStudentStockAmount(int stockId, String studentId);

    // ??? 蹂댁?ъ명?李④?
    boolean setStudentPointDown(int totalPrice, String studentId);

    // ??? 蹂댁?ъ명?利媛
    boolean setStudentPointUp(int totalPrice, String studentId);

    // 留ㅻ, 留ㅼ 二쇰Ц ?泥
    boolean setOrderRequest(OrderStatus content, int price, int amount, OrderStatus state, String studentId, int stockId);

    // ?뱀 二쇱 ?湲곗???留ㅻ 留ㅼ 二쇰Ц 紐⑤ 議고
    List<Order> getTotalOrder(int stockId);

    // ?뱀 二쇱 ?湲곗???留ㅻ 二쇰Ц 紐⑤ 議고
    List<Order> getTotalSellOrder(int stockId);

    // ?뱀 二쇱 ?湲곗???留ㅼ 二쇰Ц 紐⑤ 議고
    List<Order> getTotalBuyOrder(int stockId);

    // ??二쇰Ц ?泥 議고
    List<Order> getTotalMyOrder(int stockId, String studentId);

    // 吏?? ?깅?? 二쇰Ц?泥 no 議고
    int getMyOrderNo(OrderStatus content, String studentId, int stockId, OrderStatus state, int amount, int price);

    // 二쇱 諛? ?蹂?議고
    Map<String, Object> getStockPubInfo(int stockId);

    // 二쇱 諛? 媛? 李④?
    boolean setStockPubBalance(int buyAmount, int stockId);

    // 二쇰Ц ?泥 ?猷
    boolean setMatchedOrder(@org.apache.ibatis.annotations.Param("buyOrderId") int buyOrderId, 
                            @org.apache.ibatis.annotations.Param("sellOrderId") Integer sellOrderId,
                            @org.apache.ibatis.annotations.Param("amount") int amount,
                            @org.apache.ibatis.annotations.Param("price") int price);

    // 二쇰Ц ?泥 ?? '?湲?蹂寃?
    boolean setOrderStatePending(int orderId);

    // 二쇰Ц ?泥 ?? '泥닿껐'蹂寃?
    boolean setOrderStateMatched(int orderId);

    // 二쇰Ц ?泥 ?? '痍⑥'蹂寃?
    boolean setOrderStateCancel(int orderId);

    // 留ㅼ 二쇰Ц ?泥 留ㅼ묶
    Map<String, Object> getMatchOrder(int stockId, int orderPrice, int orderAmount, String studentId, OrderStatus content);

    List<Order> getMatchOrderList(@org.apache.ibatis.annotations.Param("stockId") int stockId,
                                  @org.apache.ibatis.annotations.Param("content") String content,
                                  @org.apache.ibatis.annotations.Param("orderPrice") int orderPrice,
                                  @org.apache.ibatis.annotations.Param("studentId") String studentId);

    void updateOrder(StockOrderResponse response);

    // 二쇰Ц 遺遺泥닿껐 ? ?⑥ ?? ??곗댄?
    boolean updateOrderAmount(@org.apache.ibatis.annotations.Param("amount") int amount,
                              @org.apache.ibatis.annotations.Param("orderId") int orderId);

    //?몄??硫?? 異媛
    StockOrderResponse getOrderById(int orderId);

    int insertOrder(Order order);
}