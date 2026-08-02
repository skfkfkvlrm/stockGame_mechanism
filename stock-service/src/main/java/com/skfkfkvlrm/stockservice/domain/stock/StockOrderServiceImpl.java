package com.skfkfkvlrm.stockservice.domain.stock;

import com.skfkfkvlrm.stockservice.client.PointClient;
import com.skfkfkvlrm.stockservice.domain.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class StockOrderServiceImpl implements StockOrderService {
    private final StockDetailRepository stockDetailRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PointClient pointClient;
    private final OrderMatcher orderMatcher = new OrderMatcher();

    public StockOrderServiceImpl(StockDetailRepository stockDetailRepository,
                                 StockPriceHistoryRepository stockPriceHistoryRepository,
                                 @org.springframework.lang.Nullable SimpMessagingTemplate messagingTemplate,
                                 PointClient pointClient) {
        this.stockDetailRepository = stockDetailRepository;
        this.stockPriceHistoryRepository = stockPriceHistoryRepository;
        this.messagingTemplate = messagingTemplate;
        this.pointClient = pointClient;
    }


    private void validateTickSize(int price) {
        int tickSize = getTickSize(price);
        if (price % tickSize != 0) {
            throw new IllegalArgumentException("올바르지 않은 호가 단위입니다.");
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
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/orders/" + stockId, "ORDER_UPDATED");
        }
    }

    @Override
    @Transactional
    public String buyStock(StockOrderRequest request) {
        validateTickSize(request.getPrice());

        Map<String, Object> stockInfo = stockDetailRepository.getStockInfo(request.getStockId());
        if (stockInfo != null) {
            String status = (String) stockInfo.get("status");
            if (status != null && !"LISTED".equalsIgnoreCase(status)) {
                if ("SUSPENDED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("해당 종목은 현재 거래가 정지되었습니다.");
                } else if ("DELISTED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("해당 종목은 상장 폐지되어 거래할 수 없습니다.");
                }
            }
        }

        int totalOrderPrice = request.getPrice() * request.getAmount();

        // OpenFeign을 통해 point-service에서 보유 포인트 조회
        ApiResponse<Integer> pointRes = pointClient.getStudentPoint(request.getStudentId());
        int currentPoints = (pointRes != null && pointRes.getData() != null) ? pointRes.getData() : 0;
        if (currentPoints < totalOrderPrice) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        // 발행 정보 확인 및 처리
        Map<String, Object> pubInfo = stockDetailRepository.getStockPubInfo(request.getStockId());
        int pubAmount = getIntOrDefault(pubInfo, "publication_balance");
        int pubPrice = getIntOrDefault(pubInfo, "publication_price");

        if (pubAmount > 0 && request.getPrice() >= pubPrice) {
            if (request.getPrice() > pubPrice) {
                throw new IllegalArgumentException("발행가보다 비싸게 매수할 수 없습니다.");
            }
            if (request.getAmount() > pubAmount) {
                throw new IllegalArgumentException("발행 잔량을 초과했습니다.");
            }

            Order order = createOrder(request, OrderStatus.매수, OrderStatus.체결);
            stockDetailRepository.insertOrder(order);
            stockDetailRepository.setMatchedOrder(order.getOrderId(), null, request.getAmount(), request.getPrice());
            stockDetailRepository.setStockPubBalance(request.getAmount(), request.getStockId());
            
            // OpenFeign point-service 포인트 차감
            pointClient.decreasePoint(request.getStudentId(), totalOrderPrice);

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), request.getPrice(), request.getAmount());

            broadcastOrderUpdate(request.getStockId());
            return "매수 주문이 체결되었습니다.";
        }

        // 학생 간 거래 매칭
        List<Order> sellOrders = stockDetailRepository.getMatchOrderList(
                request.getStockId(), OrderStatus.매도.name(), request.getPrice(), request.getStudentId());

        MatchResult matchResult = orderMatcher.match(request.getAmount(), sellOrders);

        for (MatchItem match : matchResult.getMatches()) {
            Order sellOrder = match.getCounterOrder();
            int matchAmount = match.getMatchAmount();
            int matchPrice = match.getMatchPrice();
            int matchTotalPrice = match.getMatchTotalPrice();

            int sellOrderId;
            if (match.isFullyMatched()) {
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

            Order buyFilled = Order.builder()
                    .content(OrderStatus.매수).state(OrderStatus.체결)
                    .price(matchPrice).amount(matchAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(buyFilled);
            int buyOrderId = buyFilled.getOrderId();

            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, matchPrice);
            
            // OpenFeign 포인트 차감 및 매도자 포인트 추가
            pointClient.decreasePoint(request.getStudentId(), matchTotalPrice);
            pointClient.increasePoint(sellOrder.getStudentId(), matchTotalPrice);

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), matchPrice, matchAmount);
        }

        int remainingAmount = matchResult.getRemainingAmount();

        if (remainingAmount > 0) {
            Order order = Order.builder()
                    .content(OrderStatus.매수).state(OrderStatus.대기)
                    .price(request.getPrice()).amount(remainingAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(order);
            pointClient.decreasePoint(request.getStudentId(), request.getPrice() * remainingAmount);
            
            broadcastOrderUpdate(request.getStockId());
            if (remainingAmount < request.getAmount()) {
                return "부분 체결 완료 및 남은 수량 매수 대기 등록되었습니다.";
            } else {
                return "매수 주문이 대기 등록되었습니다.";
            }
        }
        
        broadcastOrderUpdate(request.getStockId());
        return "매수 주문이 전량 체결되었습니다.";
    }

    @Override
    @Transactional
    public String sellStock(StockOrderRequest request) {
        validateTickSize(request.getPrice());

        Map<String, Object> stockInfo = stockDetailRepository.getStockInfo(request.getStockId());
        if (stockInfo != null) {
            String status = (String) stockInfo.get("status");
            if (status != null && !"LISTED".equalsIgnoreCase(status)) {
                if ("SUSPENDED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("해당 종목은 현재 거래가 정지되었습니다.");
                } else if ("DELISTED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("해당 종목은 상장 폐지되어 거래할 수 없습니다.");
                }
            }
        }

        int stockAmount = stockDetailRepository.getStudentStockAmount(request.getStockId(), request.getStudentId());
        if (request.getAmount() > stockAmount) {
            throw new IllegalArgumentException("보유 주식이 부족합니다.");
        }

        List<Order> buyOrders = stockDetailRepository.getMatchOrderList(
                request.getStockId(), OrderStatus.매수.name(), request.getPrice(), request.getStudentId());

        MatchResult matchResult = orderMatcher.match(request.getAmount(), buyOrders);

        for (MatchItem match : matchResult.getMatches()) {
            Order buyOrder = match.getCounterOrder();
            int matchAmount = match.getMatchAmount();
            int matchPrice = match.getMatchPrice();
            int matchTotalPrice = match.getMatchTotalPrice();

            int buyOrderId;
            if (match.isFullyMatched()) {
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

            Order sellFilled = Order.builder()
                    .content(OrderStatus.매도).state(OrderStatus.체결)
                    .price(matchPrice).amount(matchAmount)
                    .studentId(request.getStudentId()).stockId(request.getStockId()).build();
            stockDetailRepository.insertOrder(sellFilled);
            int sellOrderId = sellFilled.getOrderId();

            stockDetailRepository.setMatchedOrder(buyOrderId, sellOrderId, matchAmount, matchPrice);
            
            // OpenFeign 매도 대금 포인트 추가
            pointClient.increasePoint(request.getStudentId(), matchTotalPrice);

            stockPriceHistoryRepository.upsertDailyPrice(request.getStockId(), LocalDate.now(), matchPrice, matchAmount);
        }

        int remainingAmount = matchResult.getRemainingAmount();

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
        StockOrderResponse order = stockDetailRepository.getOrderById(orderId);
        if (order == null){
            throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
        }
        if (!order.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("본인 주문만 취소할 수 있습니다.");
        }
        if (order.getState() == OrderStatus.체결 || order.getState() == OrderStatus.취소) {
            throw new IllegalArgumentException("이미 처리된 주문입니다.");
        }

        String contentStr = order.getContent() != null ? order.getContent().toString() : "";
        if ("매수".equals(contentStr) || OrderStatus.매수.name().equals(contentStr)) {
            int refundAmount = order.getPrice() * order.getAmount();
            // OpenFeign 포인트 환불
            pointClient.increasePoint(studentId, refundAmount);
        }
        
        stockDetailRepository.setOrderStateCancel(orderId);
        broadcastOrderUpdate(order.getStockId());
        return order.getOrderId();
    }
}
