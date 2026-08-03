package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockOrderRequest;

public interface StockOrderService {
    /**
     * 留ㅼ 濡吏(湲곗〈 濡吏)
     * @param request 留ㅼ ?泥 ?곗댄?
     */
    String buyStock(StockOrderRequest request);
    /**
     * 留ㅻ 濡吏(湲곗〈 濡吏)
     * @param request 留ㅻ ?泥 ?곗댄?
     */
    String sellStock(StockOrderRequest request);
    /**
     * ?湲?以??二쇰Ц? 痍⑥?⑸??
     * @param orderId 痍⑥? 二쇰Ц 怨? 踰??
     * @param studentId 痍④? ?泥? 蹂대??? ID
     * @return 痍⑥? 二쇱 踰??
     */
    int cancelOrder(int orderId, String studentId);
}
