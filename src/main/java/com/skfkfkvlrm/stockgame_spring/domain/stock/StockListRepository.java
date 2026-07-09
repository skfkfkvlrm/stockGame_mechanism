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
    // 2. 주식 현재가격 조회
    // 3. 주식 이전가격, 전장마감가 조회
    // 4. 주식 이전가 대비 조회
    // 5. 주식 등락률 조회
    // 6. 주식 목록 화면용 전체 정보 조회
}
