package com.skfkfkvlrm.stockservice.exception;

public class ExceededPublicationBalanceException extends StockGameException {
    public ExceededPublicationBalanceException() {
        super(ErrorCode.EXCEEDED_PUBLICATION_BALANCE);
    }
}
