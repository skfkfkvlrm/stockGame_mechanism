package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockListRepository {
    // 1. 주식 종목명 목록 조회
    List<Stock> getStockNameList();

    // 2. 전체 주식 목록 조회 (관리자용)
    List<Stock> getAllStocks();

    // 3. 주식 신규 등록 (상장)
    int insertStock(com.skfkfkvlrm.stockservice.domain.admin.StockRequest request);

    // 4. 주식 정보 수정
    int updateStock(@org.apache.ibatis.annotations.Param("stockId") int stockId, @org.apache.ibatis.annotations.Param("request") com.skfkfkvlrm.stockservice.domain.admin.StockRequest request);

    // 5. delete stock
    int deleteStock(int stockId);

    int updateStockStatusToDelisted(int stockId);
}
