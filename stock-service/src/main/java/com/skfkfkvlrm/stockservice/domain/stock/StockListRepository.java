package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.Stock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockListRepository {
    // ???湲곗〈 ??쇰ㅼ 理?? 洹몃濡 ?ъ⑷??ν寃 ??깊? 以????
    // ?곕쇱 StockDetailDAOMybatis.java?? ??? 硫??瑜??ъъ⑺⑸??
    // 1. 二쇱紐 議고
    List<Stock> getStockNameList();

    // ?泥?二쇱 紐⑸? 議고 (愿由ъ??
    List<Stock> getAllStocks();

    // 二쇱 ?洹 ?깅?
    int insertStock(com.skfkfkvlrm.stockservice.domain.admin.StockRequest request);

    // 二쇱 ?蹂???
    int updateStock(@org.apache.ibatis.annotations.Param("stockId") int stockId, @org.apache.ibatis.annotations.Param("request") com.skfkfkvlrm.stockservice.domain.admin.StockRequest request);

    // 二쇱 ??/??ν吏
    int deleteStock(int stockId);
}
