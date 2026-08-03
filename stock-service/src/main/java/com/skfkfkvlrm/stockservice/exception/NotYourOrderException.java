package com.skfkfkvlrm.stockservice.exception;

public class NotYourOrderException extends StockGameException {
    public NotYourOrderException() {
        super(ErrorCode.NOT_YOUR_ORDER);
    }
}