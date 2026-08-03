package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockPriceResponse;

import java.util.List;

public interface StockPriceService {
    // ??ㅽ? ??λ ?泥?二쇱 醫紐⑹ ?ㅼ媛 ?????ш?, 蹂??? ?깅쎈?)
    List<StockPriceResponse> getStockPriceList();
}
