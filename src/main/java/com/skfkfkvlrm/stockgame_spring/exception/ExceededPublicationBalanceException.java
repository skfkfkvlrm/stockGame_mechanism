package com.skfkfkvlrm.stockgame_spring.exception;

public class ExceededPublicationBalanceException extends StockGameException {
    public ExceededPublicationBalanceException() {
        super(ErrorCode.EXCEEDED_PUBLICATION_BALANCE);
    }
}
