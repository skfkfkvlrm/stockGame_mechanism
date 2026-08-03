package com.skfkfkvlrm.stockservice.exception;

public class InsufficientPointException extends StockGameException {
    public InsufficientPointException() {
        super(ErrorCode.INSUFFICIENT_POINT);
    }
}