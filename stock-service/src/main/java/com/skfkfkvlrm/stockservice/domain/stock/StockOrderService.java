package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockOrderRequest;

public interface StockOrderService {
    /**
     * 매수 주문 처리
     * @param request 매수 요청 데이터
     */
    String buyStock(StockOrderRequest request);
    /**
     * 매도 주문 처리
     * @param request 매도 요청 데이터
     */
    String sellStock(StockOrderRequest request);
    /**
     * 대기 중인 주문을 취소합니다.
     * @param orderId 취소할 주문 고유 번호
     * @param studentId 취소 요청을 보낸 학생 ID
     * @return 취소된 주식 종목 번호
     */
    int cancelOrder(int orderId, String studentId);
}
