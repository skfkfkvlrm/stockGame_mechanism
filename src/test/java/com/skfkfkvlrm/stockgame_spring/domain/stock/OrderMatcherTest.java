package com.skfkfkvlrm.stockgame_spring.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMatcherTest {

    private final OrderMatcher orderMatcher = new OrderMatcher();

    @Test
    @DisplayName("대기 주문이 없을 때 체결 없이 전체 잔여 수량 반환")
    void matchEmptyCounterOrders() {
        // given
        int requestAmount = 10;
        List<Order> counterOrders = List.of();

        // when
        MatchResult result = orderMatcher.match(requestAmount, counterOrders);

        // then
        assertThat(result.hasMatches()).isFalse();
        assertThat(result.getRemainingAmount()).isEqualTo(10);
        assertThat(result.isFullyMatched()).isFalse();
    }

    @Test
    @DisplayName("단일 대기 주문 전량 체결 성공 테스트")
    void matchSingleOrderFullFill() {
        // given
        Order sellOrder = Order.builder()
                .orderId(101)
                .amount(5)
                .price(10000)
                .studentId("seller1")
                .build();
        int requestAmount = 5;

        // when
        MatchResult result = orderMatcher.match(requestAmount, List.of(sellOrder));

        // then
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getMatches()).hasSize(1);
        assertThat(result.getRemainingAmount()).isEqualTo(0);
        assertThat(result.isFullyMatched()).isTrue();

        MatchItem item = result.getMatches().get(0);
        assertThat(item.getMatchAmount()).isEqualTo(5);
        assertThat(item.getMatchPrice()).isEqualTo(10000);
        assertThat(item.isFullyMatched()).isTrue();
        assertThat(item.getMatchTotalPrice()).isEqualTo(50000);
    }

    @Test
    @DisplayName("단일 대기 주문 부분 체결 테스트")
    void matchSingleOrderPartialFill() {
        // given
        Order sellOrder = Order.builder()
                .orderId(102)
                .amount(10)
                .price(10000)
                .studentId("seller1")
                .build();
        int requestAmount = 4; // 4개만 매수 요청

        // when
        MatchResult result = orderMatcher.match(requestAmount, List.of(sellOrder));

        // then
        assertThat(result.hasMatches()).isTrue();
        assertThat(result.getMatches()).hasSize(1);
        assertThat(result.getRemainingAmount()).isEqualTo(0);
        assertThat(result.isFullyMatched()).isTrue();

        MatchItem item = result.getMatches().get(0);
        assertThat(item.getMatchAmount()).isEqualTo(4);
        assertThat(item.isFullyMatched()).isFalse(); // 10개 중 4개만 매칭되어 상대 주문은 partial
    }

    @Test
    @DisplayName("다수 대기 주문 순차 체결 및 미체결 잔량 반환 테스트")
    void matchMultipleOrdersWithRemainingBalance() {
        // given
        Order order1 = Order.builder().orderId(1).amount(3).price(1000).studentId("seller1").build();
        Order order2 = Order.builder().orderId(2).amount(4).price(1000).studentId("seller2").build();
        int requestAmount = 10; // 10개 매수 요청, 상대 7개만 존재

        // when
        MatchResult result = orderMatcher.match(requestAmount, List.of(order1, order2));

        // then
        assertThat(result.getMatches()).hasSize(2);
        assertThat(result.getRemainingAmount()).isEqualTo(3); // 10 - 7 = 3 잔량
        assertThat(result.isFullyMatched()).isFalse();

        assertThat(result.getMatches().get(0).getMatchAmount()).isEqualTo(3);
        assertThat(result.getMatches().get(0).isFullyMatched()).isTrue();

        assertThat(result.getMatches().get(1).getMatchAmount()).isEqualTo(4);
        assertThat(result.getMatches().get(1).isFullyMatched()).isTrue();
    }
}
