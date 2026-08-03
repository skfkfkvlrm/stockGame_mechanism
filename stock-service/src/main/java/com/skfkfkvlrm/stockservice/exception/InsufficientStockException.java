package com.skfkfkvlrm.stockservice.exception;

public class InsufficientStockException extends StockGameException {
    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
}