package com.skfkfkvlrm.stockservice.exception;

public class InvalidCredentialsException extends StockGameException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}