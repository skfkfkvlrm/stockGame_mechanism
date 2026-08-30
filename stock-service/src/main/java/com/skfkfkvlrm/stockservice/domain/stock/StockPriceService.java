package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockPriceResponse;

import java.util.List;

public interface StockPriceService {
    // 시스템에 등록된 전체 주식 종목의 실시간 시세(현재가, 변동액, 등락률) 조회
    List<StockPriceResponse> getStockPriceList();
}
