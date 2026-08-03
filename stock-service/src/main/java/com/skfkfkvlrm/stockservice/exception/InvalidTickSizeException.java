package com.skfkfkvlrm.stockservice.exception;

public class InvalidTickSizeException extends StockGameException {
    public InvalidTickSizeException() {
        super(ErrorCode.INVALID_TICK_SIZE);
    }
}
