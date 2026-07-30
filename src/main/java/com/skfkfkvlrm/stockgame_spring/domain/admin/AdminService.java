package com.skfkfkvlrm.stockgame_spring.domain.admin;

import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;

import java.util.List;

public interface AdminService {
    List<StudentAdminResponse> getAllStudents();
    List<Stock> getAllStocks();
    List<Coupon> getAllCoupons();
    void adjustStudentPoint(String studentId, int amount, String reason);
    java.util.Map<String, Object> getStudentDetail(String studentId);
    void createStock(StockRequest request);
    void updateStock(int stockId, StockRequest request);
    void deleteStock(int stockId);
}
