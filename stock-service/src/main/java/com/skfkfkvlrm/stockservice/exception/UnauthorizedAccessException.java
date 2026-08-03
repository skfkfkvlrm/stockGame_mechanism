package com.skfkfkvlrm.stockservice.exception;

public class UnauthorizedAccessException extends StockGameException {
    public UnauthorizedAccessException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}