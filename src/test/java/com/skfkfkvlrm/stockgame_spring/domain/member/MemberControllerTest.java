package com.skfkfkvlrm.stockgame_spring.domain.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import com.skfkfkvlrm.stockgame_spring.config.SecurityConfig;

import com.skfkfkvlrm.stockgame_spring.domain.member.StudentJoinRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentLoginRequest;
import com.skfkfkvlrm.stockgame_spring.domain.member.StudentResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void joinSuccess() throws Exception {
        // given
        StudentJoinRequest request = StudentJoinRequest.builder()
                .studentId("student1")
                .password("password")
                .name("홍길동")
                .build();
        given(memberService.join(any(StudentJoinRequest.class))).willReturn(true);

        // when & then
        mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        StudentLoginRequest request = StudentLoginRequest.builder()
                .studentId("student1")
                .password("password")
                .build();
        StudentResponse response = StudentResponse.builder()
                .studentId("student1")
                .name("홍길동")
                .build();
        given(memberService.login(any(StudentLoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value("student1"));
    }

    @Test
    @DisplayName("내 정보 조회 테스트")
    void getMeSuccess() throws Exception {
        // given (session is mocked or handled in controller)
        // using mock session
        StudentResponse info = StudentResponse.builder()
                .studentId("student1")
                .name("홍길동")
                .build();

        // when & then
        mockMvc.perform(get("/api/members/me")
                .sessionAttr("info", info))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }
}
