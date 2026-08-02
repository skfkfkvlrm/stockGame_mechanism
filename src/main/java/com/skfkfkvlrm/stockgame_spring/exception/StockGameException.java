package com.skfkfkvlrm.stockgame_spring.exception;

import lombok.Getter;

@Getter
public class StockGameException extends RuntimeException {
    private final ErrorCode errorCode;

    public StockGameException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}