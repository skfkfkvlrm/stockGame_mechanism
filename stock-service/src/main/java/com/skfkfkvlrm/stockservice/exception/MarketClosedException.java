package com.skfkfkvlrm.stockservice.exception;

public class MarketClosedException extends StockGameException {
    public MarketClosedException() {
        super(ErrorCode.MARKET_CLOSED);
    }
}
