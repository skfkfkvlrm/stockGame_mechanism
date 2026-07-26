package com.skfkfkvlrm.stockgame_spring.exception;

public class MarketClosedException extends StockGameException {
    public MarketClosedException() {
        super(ErrorCode.MARKET_CLOSED);
    }
}
