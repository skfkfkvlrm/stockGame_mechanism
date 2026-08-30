package com.skfkfkvlrm.adminservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Client 전역 Token Relay 인터셉터 설정.
 *
 * <p>관리자 UI에서 admin-service로 전달된 JWT(Authorization 헤더)를
 * Feign Client가 하위 마이크로서비스(member-service 등)를 호출할 때
 * 자동으로 동일한 헤더를 복사하여 전파(Token Relay)한다.</p>
 *
 * <p>스케줄러·비동기 작업 등 HTTP 요청 컨텍스트가 없는 경우에는
 * 인터셉터가 아무런 동작도 하지 않아(No-op) 안전하게 처리된다.</p>
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return (RequestTemplate template) -> {
            // 현재 스레드의 HTTP 요청 컨텍스트 조회 (스케줄러 등 컨텍스트 부재 시 null)
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                System.out.println("[FeignInterceptor] Request URI: " + template.url() + ", authHeader: " + (authHeader != null ? "EXISTS" : "NULL"));

                // Authorization 헤더가 존재하면 Feign 요청 템플릿에 그대로 복사
                if (authHeader != null && !authHeader.isBlank()) {
                    template.header("Authorization", authHeader);
                }
            } else {
                System.out.println("[FeignInterceptor] RequestContextHolder attributes IS NULL!");
            }
        };
    }
}
