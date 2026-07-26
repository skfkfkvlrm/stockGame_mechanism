package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockListRepository {
    // 현재 기존 파일들을 최대한 그대로 사용가능하게 작성하는 중입니다
    // 따라서 StockDetailDAOMybatis.java에서 필요한 메서드를 재사용합니다
    // 1. 주식명 조회
    List<Stock> getStockNameList();

    // 전체 주식 목록 조회 (관리자용)
    List<Stock> getAllStocks();
}
