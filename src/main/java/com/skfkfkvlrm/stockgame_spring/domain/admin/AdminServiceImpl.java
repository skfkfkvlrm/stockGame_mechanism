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
}
