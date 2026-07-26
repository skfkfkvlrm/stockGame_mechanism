package com.skfkfkvlrm.stockgame_spring.exception;

public class StockNotFoundException extends StockGameException {
    public StockNotFoundException() {
        super(ErrorCode.STOCK_NOT_FOUND);
    }
}
