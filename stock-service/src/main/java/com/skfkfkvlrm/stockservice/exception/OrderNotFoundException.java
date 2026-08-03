package com.skfkfkvlrm.stockservice.exception;

public class OrderNotFoundException extends StockGameException {
    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
}