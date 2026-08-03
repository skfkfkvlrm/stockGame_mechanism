package com.skfkfkvlrm.stockservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 400 Bad Request
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "?ъ명멸? 遺議깊⑸??"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "蹂댁 二쇱??遺議깊⑸??"),
    INVALID_ORDER_STATE(HttpStatus.BAD_REQUEST, "泥由ы ? ?? 二쇰Ц ??????"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "?紐삳 ??κ?????"),
    MARKET_CLOSED(HttpStatus.BAD_REQUEST, "???二쇱 ??μ???λ??듬??"),
    INVALID_TICK_SIZE(HttpStatus.BAD_REQUEST, "?щ?瑜댁? ?? ?멸? ?⑥????"),
    EXCEEDED_PUBLICATION_BALANCE(HttpStatus.BAD_REQUEST, "諛? ??蹂대?留? ??? 留ㅼ? ? ??듬??"),
    INVALID_PUBLICATION_PRICE(HttpStatus.BAD_REQUEST, "??ㅽ 諛? 媛寃⑸낫???? 媛寃⑹쇰? 二쇰Ц? ? ??듬??"),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "?대??ъ⑸ 荑?곗닿굅? 蹂몄??? 荑?곗??????"),
    
    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "濡洹몄몄????⑸??"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "??대 ?? 鍮諛踰?멸? ?щ?瑜댁? ??듬??"),

    // 403 Forbidden
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "?洹?沅?????듬??"),
    NOT_YOUR_ORDER(HttpStatus.FORBIDDEN, "蹂몄?二쇰Ц???????"),

    // 404 Not Found
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "議댁ы吏 ?? 二쇰Ц????"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "議댁ы吏 ?? ?ъ⑹????"),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "議댁ы吏 ?? 醫紐⑹???"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "?踰 ?대? ?ㅻ?媛 諛???듬??");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}