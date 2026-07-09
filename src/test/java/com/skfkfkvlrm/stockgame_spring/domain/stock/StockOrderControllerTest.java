package com.skfkfkvlrm.stockgame_spring.domain.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import com.skfkfkvlrm.stockgame_spring.domain.stock.StockOrderRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class StockOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockOrderService stockOrderService;

    @Test
    @DisplayName("주식 매수 성공 테스트")
    void buyStockSuccess() throws Exception {
        // given
        StockOrderRequest request = StockOrderRequest.builder()
                .stockId(1)
                .amount(10)
                .price(100) // add price to bypass @Min validation
                .build();
        given(stockOrderService.buyStock(any(StockOrderRequest.class))).willReturn("매수 성공");

        // when & then
        mockMvc.perform(post("/api/orders/buy")
                .sessionAttr("studentId", "student1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
