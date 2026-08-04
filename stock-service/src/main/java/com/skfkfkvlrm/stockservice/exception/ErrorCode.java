package com.skfkfkvlrm.stockservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "보유 포인트가 부족합니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "보유 주식 수가 부족합니다."),
    INVALID_ORDER_STATE(HttpStatus.BAD_REQUEST, "처리할 수 없는 주문 상태입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    MARKET_CLOSED(HttpStatus.BAD_REQUEST, "현재 주식 시장이 마감되었습니다."),
    INVALID_TICK_SIZE(HttpStatus.BAD_REQUEST, "올바르지 않은 호가 단위입니다."),
    EXCEEDED_PUBLICATION_BALANCE(HttpStatus.BAD_REQUEST, "발행 잔여 수량보다 많은 수량을 매수할 수 없습니다."),
    INVALID_PUBLICATION_PRICE(HttpStatus.BAD_REQUEST, "시스템 발행 가격보다 낮은 가격으로 주문할 수 없습니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰이거나 본인 소유의 쿠폰이 아닙니다."),
    STOCK_SUSPENDED(HttpStatus.BAD_REQUEST, "해당 종목은 현재 거래가 정지되었습니다."),
    STOCK_DELISTED(HttpStatus.BAD_REQUEST, "해당 종목은 상장 폐지되어 거래할 수 없습니다."),
    
    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_YOUR_ORDER(HttpStatus.FORBIDDEN, "본인 주문이 아닙니다."),

    // 404 Not Found
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 종목입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}