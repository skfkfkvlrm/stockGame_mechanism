package com.skfkfkvlrm.stockgame_spring.domain.admin;

import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;

import java.util.List;

public interface AdminService {
    List<StudentAdminResponse> getAllStudents();
    List<Stock> getAllStocks();
    List<Coupon> getAllCoupons();
}
