package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.skfkfkvlrm.stockgame_spring.domain.stock.StockOrderRequest;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockOrderResponse;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Order;
import com.skfkfkvlrm.stockgame_spring.domain.stock.OrderStatus;
import com.skfkfkvlrm.stockgame_spring.domain.admin.MarketSettingsRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockPriceHistoryRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockOrderServiceImpl implements StockOrderService {
    private final StockDetailRepository stockDetailRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final MarketSettingsRepository marketSettingsRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private void validateMarketOpen() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings != null && !settings.isMarketOpen()) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.MarketClosedException();
        }
    }

    private void validateTickSize(int price) {
        int tickSize = getTickSize(price);
        if (price % tickSize != 0) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InvalidTickSizeException();
        }
    }

    private int getTickSize(int price) {
        if (price < 1000) return 1;
        if (price < 5000) return 5;
        if (price < 10000) return 10;
        if (price < 50000) return 50;
        return 100;
    }

    private void broadcastOrderUpdate(int stockId) {
        messagingTemplate.convertAndSend("/topic/orders/" + stockId, "ORDER_UPDATED");
    }

    private void notifyStudent(String studentId, String message) {
        messagingTemplate.convertAndSendToUser(studentId, "/queue/notifications", message);
    }

    @Override
    @Transactional
    public String buyStock(StockOrderRequest request) {
        validateMarketOpen();
        validateTickSize(request.getPrice());

        int totalOrderPrice = request.getPrice() * request.getAmount();
        // 1. 보유 포인트 확인
        int currentPoints = stockDetailRepository.getStudentPoint(request.getStudentId());
        if (currentPoints < totalOrderPrice) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InsufficientPointException();
        }
        // 2. 발행 정보 확인
        Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(request.getStockId());
        int pubAmount = getIntOrDefault(pubInfo, "publication_balance");
        int pubPrice = getIntOrDefault(pubInfo, "publication_price");
        // a. 발행 주식 거래 (매수 가격이 발행가와 같을 때만)
        if (pubAmount > 0 && request.getPrice() >= pubPrice) {
            if (request.getPrice() > pubPrice) {
                throw new com.skfkfkvlrm.stockgame_spring.exception.InvalidPublicationPriceException();
            }
            if (request.getAmount() > pubAmount) {
                throw new com.skfkfkvlrm.stockgame_spring.exception.ExceededPublicationBalanceException();
            }

            Order order = createOrder(request, OrderStatus.매수, OrderStatus.체결);
            stockDetailRepository.insertOrder(order);
            stockDetailRepository.setMatchedOrder(order.getOrderId(), null, request.getAmount(), request.getPrice());
            stockDetailRepository.setStockPubBalance(request.getAmount(), request.getStockId());
            stockDetailRepository.setStudentPointDown(totalOrderPrice, request.getStudentId());

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), request.getPrice(), request.getAmount());

            broadcastOrderUpdate(request.getStockId());
            notifyStudent(request.getStudentId(), request.getStockId() + " 종목 매수가 체결되었습니다.");

            return "매수 주문이 체결되었습니다.";
        }
        // b. 학생 간 거래 (부분 체결 로직)
        int remainingAmount = request.getAmount();
        // 매수 시 "매도" 대기열을 조회
        List<Order> sellOrders = stockDetailRepository.getMatchOrderList(
                request.getStockId(), OrderStatus.매도.name(), request.getPrice(), request.getStudentId());

        for (Order sellOrder : sellOrders) {
            int matchAmount = Math.min(remainingAmount, sellOrder.getAmount());
            int matchPrice = sellOrder.getPrice();
            int matchTotalPrice = matchPrice * matchAmount;

            // 매도 주문 처리
            int sellOrderId;
            if (matchAmount == sellOrder.getAmount()) {
                stockDetailRepository.setOrderStateMatched(sellOrder.getOrderId());
                sellOrderId = sellOrder.getOrderId();
            } else {
                stockDetailRepository.updateOrderAmount(sellOrder.getAmount() - matchAmount, sellOrder.getOrderId());
                Order sellFilled = Order.builder()
                        .content(OrderStatus.매도).state(OrderStatus.체결)
                        .price(matchPrice).amount(matchAmount)
                        .studentId(sellOrder.getStudentId()).stockId(request.getStockId()).build();
                stockDetailRepository.insertOrder(sellFilled);
                sellOrderId = sellFilled.getOrderId();
            }

            // 매수 주문 처리 (체결용)
            Order buyFilled = Order.builder()
                    .content(OrderStatus.매수).state(OrderStatus.체결)
                    .price(matchPrice).amount(matchAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(buyFilled);
            int buyOrderId = buyFilled.getOrderId();

            // 거래내역 및 포인트 정산
            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, matchPrice);
            stockDetailRepository.setStudentPointDown(matchTotalPrice, request.getStudentId());
            stockDetailRepository.setStudentPointUp(matchTotalPrice, sellOrder.getStudentId());

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), matchPrice, matchAmount);

            remainingAmount -= matchAmount;
            if (remainingAmount == 0) {
                break;
            }
        }

        // c. 남은 수량이 있으면 대기 등록
        if (remainingAmount > 0) {
            Order order = Order.builder()
                    .content(OrderStatus.매수).state(OrderStatus.대기)
                    .price(request.getPrice()).amount(remainingAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(order);
            stockDetailRepository.setStudentPointDown(request.getPrice() * remainingAmount, request.getStudentId());
            
            broadcastOrderUpdate(request.getStockId());
            if (remainingAmount < request.getAmount()) {
                return "부분 체결 완료 및 남은 수량 매수 대기 등록되었습니다.";
            } else {
                return "매수 주문이 대기 등록되었습니다.";
            }
        }
        
        broadcastOrderUpdate(request.getStockId());
        notifyStudent(request.getStudentId(), request.getStockId() + " 종목 매수가 전량 체결되었습니다.");
        return "매수 주문이 전량 체결되었습니다.";
    }

    @Override
    @Transactional
    public String sellStock(StockOrderRequest request) {
        validateMarketOpen();
        validateTickSize(request.getPrice());

        Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(request.getStockId());
        // 발행 잔량(pubAmount)이 남아 있어도 매도(예약)는 가능하도록 방어 로직 제거
        
        // 1. 보유 주식 수량 검증
        int stockAmount = stockDetailRepository.getStudentStockAmount(request.getStockId(), request.getStudentId());
        if (request.getAmount() > stockAmount) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InsufficientStockException();
        }
        int totalOrderPrice = request.getPrice() * request.getAmount();
        // 2. 학생 간 거래 (부분 체결 로직)
        int remainingAmount = request.getAmount();
        // 매도 시 "매수" 대기열을 조회
        List<Order> buyOrders = stockDetailRepository.getMatchOrderList(
                request.getStockId(), OrderStatus.매수.name(), request.getPrice(), request.getStudentId());

        for (Order buyOrder : buyOrders) {
            int matchAmount = Math.min(remainingAmount, buyOrder.getAmount());
            int matchPrice = buyOrder.getPrice();
            int matchTotalPrice = matchPrice * matchAmount;

            // 매수 주문 처리
            int buyOrderId;
            if (matchAmount == buyOrder.getAmount()) {
                stockDetailRepository.setOrderStateMatched(buyOrder.getOrderId());
                buyOrderId = buyOrder.getOrderId();
            } else {
                stockDetailRepository.updateOrderAmount(buyOrder.getAmount() - matchAmount, buyOrder.getOrderId());
                Order buyFilled = Order.builder()
                        .content(OrderStatus.매수).state(OrderStatus.체결)
                        .price(matchPrice).amount(matchAmount)
                        .studentId(buyOrder.getStudentId()).stockId(request.getStockId()).build();
                stockDetailRepository.insertOrder(buyFilled);
                buyOrderId = buyFilled.getOrderId();
            }

            // 매도 주문 처리 (체결용)
            Order sellFilled = Order.builder()
                    .content(OrderStatus.매도).state(OrderStatus.체결)
                    .price(matchPrice).amount(matchAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(sellFilled);
            int sellOrderId = sellFilled.getOrderId();

            // 거래내역 및 포인트 정산
            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, matchPrice);
            stockDetailRepository.setStudentPointUp(matchTotalPrice, request.getStudentId());

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), matchPrice, matchAmount);

            remainingAmount -= matchAmount;
            if (remainingAmount == 0) {
                break;
            }
        }

        // 3. 남은 수량이 있으면 대기 등록
        if (remainingAmount > 0) {
            Order order = Order.builder()
                    .content(OrderStatus.매도).state(OrderStatus.대기)
                    .price(request.getPrice()).amount(remainingAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(order);
            
            broadcastOrderUpdate(request.getStockId());
            if (remainingAmount < request.getAmount()) {
                return "부분 체결 완료 및 남은 수량 매도 대기 등록되었습니다.";
            } else {
                return "매도 주문이 대기 등록되었습니다.";
            }
        }

        broadcastOrderUpdate(request.getStockId());
        notifyStudent(request.getStudentId(), request.getStockId() + " 종목 매도가 전량 체결되었습니다.");
        return "매도 주문이 전량 체결되었습니다.";
    }

    private Order createOrder(StockOrderRequest request, OrderStatus content, OrderStatus state) {
        return Order.builder()
                .content(content)
                .state(state.equals(OrderStatus.체결) ? OrderStatus.체결 : OrderStatus.대기)
                .price(request.getPrice())
                .amount(request.getAmount())
                .studentId(request.getStudentId())
                .stockId(request.getStockId())
                .build();
    }

    private int getIntOrDefault(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return 0;
        }
        return ((Number) map.get(key)).intValue();
    }

    @Override
    @Transactional
    public int cancelOrder(int orderId, String studentId) {
        // 1. 취소할 주문 정보 상세 조회
        StockOrderResponse order = stockDetailRepository.getOrderById(orderId);
        if (order == null){
            throw new com.skfkfkvlrm.stockgame_spring.exception.OrderNotFoundException();
        }
        // 2. 본인 주문이 맞는지 검증
        if (!order.getStudentId().equals(studentId)) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.NotYourOrderException();
        }
        // 3. 주문 상태가 취소 가능한 상태('대기')인지 검증
        if (order.getState() == OrderStatus.체결) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InvalidOrderStateException();
        }
        if (order.getState() == OrderStatus.취소) {
            throw new com.skfkfkvlrm.stockgame_spring.exception.InvalidOrderStateException();
        }
        // 4. 매수 취소 시 포인트 환불
        // order.getContent()는 DB에 한국어("매수")로 저장되므로 OrderStatus enum 이름으로 비교해야 함
        if (OrderStatus.매수.name().equals(order.getContent())) {
            int refundAmount = order.getPrice() * order.getAmount();
            stockDetailRepository.setStudentPointUp(refundAmount, studentId);
        }
        // 5. 주문 상태를 '취소'로 업데이트
        stockDetailRepository.setOrderStateCancel(orderId);
        broadcastOrderUpdate(order.getStockId());
        // 6. 주식 번호 리턴
        return order.getOrderId();
    }
}
