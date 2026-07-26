package com.skfkfkvlrm.stockgame_spring.domain.admin;

import com.skfkfkvlrm.stockgame_spring.auth.AppUserDetailsService;
import com.skfkfkvlrm.stockgame_spring.auth.JwtFilter;
import com.skfkfkvlrm.stockgame_spring.auth.JwtUtil;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.Coupon;
import com.skfkfkvlrm.stockgame_spring.domain.coupon.CouponRepository;
import com.skfkfkvlrm.stockgame_spring.domain.member.MemberRepository;
import com.skfkfkvlrm.stockgame_spring.domain.news.NewsRepository;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyAssetRepository;
import com.skfkfkvlrm.stockgame_spring.domain.point.MyPointHistoryRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.Stock;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockDetailRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockListRepository;
import com.skfkfkvlrm.stockgame_spring.domain.stock.StockPriceHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    // All 8 MyBatis Mappers mocked to prevent SqlSessionFactory requirements during slice testing
    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private StockListRepository stockListRepository;

    @MockBean
    private StockDetailRepository stockDetailRepository;

    @MockBean
    private StockPriceHistoryRepository stockPriceHistoryRepository;

    @MockBean
    private CouponRepository couponRepository;

    @MockBean
    private MyAssetRepository myAssetRepository;

    @MockBean
    private MyPointHistoryRepository myPointHistoryRepository;

    @MockBean
    private NewsRepository newsRepository;

    @MockBean
    private MarketSettingsRepository marketSettingsRepository;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("학생 목록 데이터 조회 성공 테스트")
    void getStudentsSuccess() throws Exception {
        // given
        StudentAdminResponse student = StudentAdminResponse.builder()
                .id(1)
                .studentId("2026001")
                .name("김철수")
                .grade(1)
                .className("1반")
                .classNumber(5)
                .totalPoint(30000)
                .totalCoupon(0)
                .build();
        given(adminService.getAllStudents()).willReturn(List.of(student));

        // when & then
        mockMvc.perform(get("/api/admin/students")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].studentId").value("2026001"))
                .andExpect(jsonPath("$.data[0].name").value("김철수"));
    }

    @Test
    @DisplayName("주식 종목 목록 데이터 조회 성공 테스트")
    void getStocksSuccess() throws Exception {
        // given
        Stock stock = Stock.builder()
                .stockId(1)
                .name("삼성전자")
                .content("IT/반도체")
                .publicationPrice(70000)
                .publicationBalance(1000)
                .build();
        given(adminService.getAllStocks()).willReturn(List.of(stock));

        // when & then
        mockMvc.perform(get("/api/admin/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("삼성전자"));
    }

    @Test
    @DisplayName("쿠폰 목록 데이터 조회 성공 테스트")
    void getCouponsSuccess() throws Exception {
        // given
        Coupon coupon = Coupon.builder()
                .couponId(1)
                .name("매점 1천원 할인권")
                .price(1000)
                .build();
        given(adminService.getAllCoupons()).willReturn(List.of(coupon));

        // when & then
        mockMvc.perform(get("/api/admin/coupons")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("매점 1천원 할인권"));
    }
}
