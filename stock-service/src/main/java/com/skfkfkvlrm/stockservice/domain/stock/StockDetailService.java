package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockDetailResponse;
import com.skfkfkvlrm.stockservice.domain.stock.Order;

import java.util.List;

public interface StockDetailService {
    // 주식 기본 정보 및 주요 지표 조회
    StockDetailResponse getStockDetailInfo(int stockId);
    // 등록된 실시간 주문 목록 조회
    List<Order> getLiveOrderList(int stockId, String type);
    // 내 대기 주문 목록 조회
    List<Order> getwaitingOrderList(int stockId, String studentId);
    // 전체 주식 목록 일괄 조회
    List<StockDetailResponse> getAllStocks();
    // KOSPI / KOSDAQ 시장 지수 데이터 조회
    List<MarketIndexResponse> getMarketIndices();
    // 상장폐지 청산 파이프라인
    void delistStock(int stockId, int compensationPrice, String reason);
}
