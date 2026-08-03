package com.skfkfkvlrm.stockservice.exception;

public class StockNotFoundException extends StockGameException {
    public StockNotFoundException() {
        super(ErrorCode.STOCK_NOT_FOUND);
    }
}
