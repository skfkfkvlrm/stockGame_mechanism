package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.domain.stock.StockOrderRequest;
import com.skfkfkvlrm.stockservice.domain.stock.StockOrderResponse;
import com.skfkfkvlrm.stockservice.domain.admin.MarketSettings;
import com.skfkfkvlrm.stockservice.domain.stock.Order;
import com.skfkfkvlrm.stockservice.domain.stock.OrderStatus;
import com.skfkfkvlrm.stockservice.domain.admin.MarketSettingsRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockPriceHistoryRepository;
import com.skfkfkvlrm.stockservice.domain.stock.StockOrderService;
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
    private final OrderMatcher orderMatcher = new OrderMatcher();

    private void validateMarketOpen() {
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        if (settings != null && !settings.calculateIsMarketOpen()) {
            throw new com.skfkfkvlrm.stockservice.exception.MarketClosedException();
        }
    }

    private void validateTickSize(int price) {
        // 1포인트(1원) 단위 거래 제한 해제: 1 이상의 정수 금액은 모두 자유 주문 허용
        if (price <= 0) {
            throw new com.skfkfkvlrm.stockservice.exception.InvalidTickSizeException();
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

        Map<String, Object> stockInfo = stockDetailRepository.getStockInfo(request.getStockId());
        if (stockInfo != null) {
            String status = (String) stockInfo.get("status");
            if (status != null && !"LISTED".equalsIgnoreCase(status)) {
                if ("SUSPENDED".equalsIgnoreCase(status)) {
                    throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.STOCK_SUSPENDED);
                } else if ("DELISTED".equalsIgnoreCase(status)) {
                    throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.STOCK_DELISTED);
                }
            }
        }

        int totalOrderPrice = request.getPrice() * request.getAmount();
        // 1. 보유 포인트 확인
        Integer pointsObj = stockDetailRepository.getStudentPoint(request.getStudentId());
        int currentPoints;
        if (pointsObj == null) {
            if ("admin".equals(request.getStudentId())) {
                currentPoints = 99999999;
            } else {
                throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.USER_NOT_FOUND);
            }
        } else {
            currentPoints = pointsObj;
        }
        if (currentPoints < totalOrderPrice) {
            throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.INSUFFICIENT_POINT);
        }
        // 시장 상태 조회
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        boolean isCallAuction = settings != null && "CALL_AUCTION".equalsIgnoreCase(settings.calculateStatusCode());

        // 2. 동시호가 기간(CALL_AUCTION)인 경우 즉시 체결하지 않고 대기(WAITING)로만 적재
        if (isCallAuction) {
            Order order = Order.builder()
                    .content(OrderStatus.BUY).state(OrderStatus.WAITING)
                    .price(request.getPrice()).amount(request.getAmount())
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(order);
            stockDetailRepository.setStudentPointDown(totalOrderPrice, request.getStudentId());

            broadcastOrderUpdate(request.getStockId());
            return "장 마감 동시호가 매수 주문이 접수되었습니다. (15:30에 일괄 체결됩니다)";
        }

        // 3. 정규장 연속매매: 오더북(Order Book) 기반 지정가 매칭
        List<Order> sellOrders = stockDetailRepository.getMatchOrderList(
                request.getStockId(), OrderStatus.SELL.name(), request.getPrice(), request.getStudentId());

        MatchResult matchResult = orderMatcher.match(request.getAmount(), sellOrders);

        for (MatchItem match : matchResult.getMatches()) {
            Order sellOrder = match.getCounterOrder();
            int matchAmount = match.getMatchAmount();
            int matchPrice = match.getMatchPrice();
            int matchTotalPrice = match.getMatchTotalPrice();

            // 매도 주문 처리
            int sellOrderId;
            if (match.isFullyMatched()) {
                stockDetailRepository.setOrderStateMatched(sellOrder.getOrderId());
                sellOrderId = sellOrder.getOrderId();
            } else {
                stockDetailRepository.updateOrderAmount(sellOrder.getAmount() - matchAmount, sellOrder.getOrderId());
                Order sellFilled = Order.builder()
                        .content(OrderStatus.SELL).state(OrderStatus.MATCHED)
                        .price(matchPrice).amount(matchAmount)
                        .studentId(sellOrder.getStudentId()).stockId(request.getStockId()).build();
                stockDetailRepository.insertOrder(sellFilled);
                sellOrderId = sellFilled.getOrderId();
            }

            // 매수 주문 처리 (체결용)
            Order buyFilled = Order.builder()
                    .content(OrderStatus.BUY).state(OrderStatus.MATCHED)
                    .price(matchPrice).amount(matchAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(buyFilled);
            int buyOrderId = buyFilled.getOrderId();

            // 거래내역 및 포인트 정산
            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, matchPrice);
            stockDetailRepository.setStudentPointDown(matchTotalPrice, request.getStudentId());
            
            // 일반 학생 매도자인 경우에만 포인트 입금 (SYSTEM_LP는 시스템 공급자)
            if (!"SYSTEM_LP".equals(sellOrder.getStudentId())) {
                stockDetailRepository.setStudentPointUp(matchTotalPrice, sellOrder.getStudentId());
            }

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), matchPrice, matchAmount);
        }

        int remainingAmount = matchResult.getRemainingAmount();

        // c. 남은 수량이 있으면 대기 등록
        if (remainingAmount > 0) {
            Order order = Order.builder()
                    .content(OrderStatus.BUY).state(OrderStatus.WAITING)
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

        Map<String, Object> stockInfo = stockDetailRepository.getStockInfo(request.getStockId());
        if (stockInfo != null) {
            String status = (String) stockInfo.get("status");
            if (status != null && !"LISTED".equalsIgnoreCase(status)) {
                if ("SUSPENDED".equalsIgnoreCase(status)) {
                    throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.STOCK_SUSPENDED);
                } else if ("DELISTED".equalsIgnoreCase(status)) {
                    throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.STOCK_DELISTED);
                }
            }
        }

        Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(request.getStockId());
        
        // 1. 보유 주식 수량 검증
        int stockAmount = stockDetailRepository.getStudentStockAmount(request.getStockId(), request.getStudentId());
        if (request.getAmount() > stockAmount) {
            throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.INSUFFICIENT_STOCK);
        }

        // 시장 상태 조회
        MarketSettings settings = marketSettingsRepository.findById(1).orElse(null);
        boolean isCallAuction = settings != null && "CALL_AUCTION".equalsIgnoreCase(settings.calculateStatusCode());

        // 2. 동시호가 기간(CALL_AUCTION)인 경우 즉시 체결하지 않고 대기(WAITING)로만 적재
        if (isCallAuction) {
            Order order = Order.builder()
                    .content(OrderStatus.SELL).state(OrderStatus.WAITING)
                    .price(request.getPrice()).amount(request.getAmount())
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(order);

            broadcastOrderUpdate(request.getStockId());
            return "장 마감 동시호가 매도 주문이 접수되었습니다. (15:30에 일괄 체결됩니다)";
        }

        // 3. 정규장 연속매매: 학생 간 거래 (부분 체결 로직)
        List<Order> buyOrders = stockDetailRepository.getMatchOrderList(
                request.getStockId(), OrderStatus.BUY.name(), request.getPrice(), request.getStudentId());

        MatchResult matchResult = orderMatcher.match(request.getAmount(), buyOrders);

        for (MatchItem match : matchResult.getMatches()) {
            Order buyOrder = match.getCounterOrder();
            int matchAmount = match.getMatchAmount();
            int matchPrice = match.getMatchPrice();
            int matchTotalPrice = match.getMatchTotalPrice();

            // 매수 주문 처리
            int buyOrderId;
            if (match.isFullyMatched()) {
                stockDetailRepository.setOrderStateMatched(buyOrder.getOrderId());
                buyOrderId = buyOrder.getOrderId();
            } else {
                stockDetailRepository.updateOrderAmount(buyOrder.getAmount() - matchAmount, buyOrder.getOrderId());
                Order buyFilled = Order.builder()
                        .content(OrderStatus.BUY).state(OrderStatus.MATCHED)
                        .price(matchPrice).amount(matchAmount)
                        .studentId(buyOrder.getStudentId()).stockId(request.getStockId()).build();
                stockDetailRepository.insertOrder(buyFilled);
                buyOrderId = buyFilled.getOrderId();
            }

            // 매도 주문 처리 (체결용)
            Order sellFilled = Order.builder()
                    .content(OrderStatus.SELL).state(OrderStatus.MATCHED)
                    .price(matchPrice).amount(matchAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(sellFilled);
            int sellOrderId = sellFilled.getOrderId();

            // 거래내역 및 포인트 정산
            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, matchPrice);
            stockDetailRepository.setStudentPointUp(matchTotalPrice, request.getStudentId());

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), matchPrice, matchAmount);
        }

        int remainingAmount = matchResult.getRemainingAmount();

        // 4. 남은 수량이 있으면 대기 등록
        if (remainingAmount > 0) {
            Order order = Order.builder()
                    .content(OrderStatus.SELL).state(OrderStatus.WAITING)
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
                .state(state.equals(OrderStatus.MATCHED) ? OrderStatus.MATCHED : OrderStatus.WAITING)
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
            throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.ORDER_NOT_FOUND);
        }
        // 2. 본인 주문이 맞는지 검증
        if (!order.getStudentId().equals(studentId)) {
            throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.NOT_YOUR_ORDER);
        }
        // 3. 주문 상태가 취소 가능한 상태('대기')인지 검증
        if (order.getState() == OrderStatus.MATCHED || order.getState() == OrderStatus.CANCELLED) {
            throw new com.skfkfkvlrm.stockservice.exception.StockGameException(com.skfkfkvlrm.stockservice.exception.ErrorCode.INVALID_ORDER_STATE);
        }
        // 4. 매수 취소 시 포인트 환불
        String contentStr = order.getContent() != null ? order.getContent().toString() : "";
        if ("BUY".equals(contentStr) || OrderStatus.BUY.name().equals(contentStr)) {
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
