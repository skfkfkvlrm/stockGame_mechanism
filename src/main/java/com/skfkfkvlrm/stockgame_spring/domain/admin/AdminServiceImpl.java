package com.skfkfkvlrm.stockgame_spring.domain.admin;

import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponRepository;
import com.skfkfkvlrm.stockgame_spring.domain.member.MemberRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final StockListRepository stockListRepository;
    private final CouponRepository couponRepository;

    private final com.skfkfkvlrm.stockgame_spring.domain.point.MyAssetService myAssetService;

    @Override
    public List<StudentAdminResponse> getAllStudents() {
        return memberRepository.getAllStudents();
    }

    @Override
    public List<Stock> getAllStocks() {
        return stockListRepository.getAllStocks();
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.getCouponList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void adjustStudentPoint(String studentId, int amount, String reason) {
        memberRepository.updateStudentPoint(studentId, amount);
        String historyContent = (reason != null && !reason.trim().isEmpty()) 
                ? reason 
                : (amount >= 0 ? "관리자 특별 포인트 지급" : "관리자 포인트 차감");
        memberRepository.insertPointHistory(studentId, amount, historyContent);
    }

    @Override
    public java.util.Map<String, Object> getStudentDetail(String studentId) {
        java.util.Map<String, Object> detail = new java.util.HashMap<>();
        var dashboard = myAssetService.getDashboard(studentId);
        detail.put("studentId", studentId);
        detail.put("dashboard", dashboard);
        return detail;
    }

    @Override
    public void createStock(StockRequest request) {
        if (request.getPublicationPrice() <= 0 || request.getPublicationBalance() < 0) {
            throw new IllegalArgumentException("발행 가격은 1원 이상, 발행 수량은 0개 이상이어야 합니다.");
        }
        stockListRepository.insertStock(request);
    }

    @Override
    public void updateStock(int stockId, StockRequest request) {
        if (request.getPublicationPrice() <= 0 || request.getPublicationBalance() < 0) {
            throw new IllegalArgumentException("발행 가격은 1원 이상, 발행 수량은 0개 이상이어야 합니다.");
        }
        stockListRepository.updateStock(stockId, request);
    }

    @Override
    public void deleteStock(int stockId) {
        stockListRepository.deleteStock(stockId);
    }

    @Override
    public void createCoupon(CouponRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("쿠폰 상품명을 입력해 주세요.");
        }
        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("쿠폰 판매 가격은 1P 이상이어야 합니다.");
        }
        couponRepository.insertCoupon(request);
    }

    @Override
    public void updateCoupon(int couponId, CouponRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("쿠폰 상품명을 입력해 주세요.");
        }
        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("쿠폰 판매 가격은 1P 이상이어야 합니다.");
        }
        couponRepository.updateCoupon(couponId, request);
    }

    @Override
    public void deleteCoupon(int couponId) {
        couponRepository.deleteCoupon(couponId);
    }
}
