package com.skfkfkvlrm.stockgame_spring.exception;

public class InvalidTickSizeException extends StockGameException {
    public InvalidTickSizeException() {
        super(ErrorCode.INVALID_TICK_SIZE);
    }
}
