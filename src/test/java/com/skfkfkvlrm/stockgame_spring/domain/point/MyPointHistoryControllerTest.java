package com.skfkfkvlrm.stockgame_spring.domain.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyPointHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class MyPointHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPointHistoryRepository pointHistoryRepository;

    @Test
    @DisplayName("포인트 내역 조회 테스트 (로그인 성공)")
    void getHistorySuccess() throws Exception {
        // given
        PointHistoryResponse mockResponse = PointHistoryResponse.builder()
                .historyType("출석")
                .pointChange(100)
                .build();
        given(pointHistoryRepository.getMyPointHistoryList(anyString())).willReturn(List.of(mockResponse));

        // when & then
        mockMvc.perform(get("/api/history")
                .sessionAttr("studentId", "student1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].historyType").value("출석"))
                .andExpect(jsonPath("$.data[0].pointChange").value(100));
    }

    @Test
    @DisplayName("포인트 내역 조회 테스트 (로그인 안됨)")
    void getHistoryFailNoSession() throws Exception {
        // when & then
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }
}
