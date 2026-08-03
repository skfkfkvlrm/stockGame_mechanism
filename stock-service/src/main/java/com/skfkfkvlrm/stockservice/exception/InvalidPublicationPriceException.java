package com.skfkfkvlrm.stockservice.exception;

public class InvalidPublicationPriceException extends StockGameException {
    public InvalidPublicationPriceException() {
        super(ErrorCode.INVALID_PUBLICATION_PRICE);
    }
}
