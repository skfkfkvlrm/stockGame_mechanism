# 포트폴리오 맥락 분석 리포트

> 직접 소스 코드 열람 기반 분석 | 2026-07-26  
> 대상 프로젝트: `stockGame`, `stockGame_spring`, `stockGame_react`

---

## 1. 직접 열람한 참고 자료 목록

| 구분 | 파일 | 핵심 내용 |
|------|------|-----------|
| 구버전 (Servlet/JSP) | `stockGame/FrontControllerServlet.java` | 직접 구현한 Front Controller 패턴 |
| 비즈니스 핵심 로직 | `stockGame_spring/StockOrderServiceImpl.java` | 부분 체결·대기 등록·호가 단위 검증·WebSocket 알림 |
| 스케줄러 | `stockGame_spring/StockSchedulerService.java` | `@Scheduled` 매일 자정 전일 종가 동기화 |
| 도메인 엔티티 | `stockGame_spring/Student.java` | JPA `@Entity` 설계 |
| API 컨트롤러 | `stockGame_spring/StockOrderController.java` | JWT 기반 `studentId` 추출, REST API |
| 예외 처리 | `GlobalExceptionHandler.java` + `ErrorCode.java` | 도메인 예외 → HTTP 상태코드 일관 매핑 |
| 테스트 코드 | `StockOrderControllerTest.java` | `@WebMvcTest`, `MockMvc`, BDD 스타일 |
| 프론트엔드 | `stockGame_react/StockDetail.jsx` | STOMP 실시간 시세·호가창·캔들차트 |

---

## 2. 기술 진화 흐름 (3단계 리팩터링 증거)

```
[stockGame]                     [stockGame_spring]                  [stockGame_react]
Servlet/JSP                  →  Spring Boot                      →  React + Vite
FrontControllerServlet           StockOrderServiceImpl                StockDetail.jsx
 └ cmd 파라미터 라우팅             └ @Transactional 비즈니스 로직        └ useStompResilience 훅
 └ request.getAttribute()         └ JWT Filter 인증                    └ Zustand 전역 인증 상태
 └ JSP forward/redirect           └ ErrorCode Enum 예외 체계            └ ApexCharts 캔들차트
 └ ActionFactory 패턴              └ @Scheduled 배치                    └ Promise.all() 병렬 호출
```

**핵심 포인트:** 단순히 프레임워크를 갈아탄 것이 아니라, 매 단계에서 **이전 방식의 한계를 직접 체험하고 개선**한 흐름이 코드로 증명됨.

---

## 3. 주요 기술 증거 상세

### 3-1. 주문 매칭 로직 (`StockOrderServiceImpl.java`)

| 케이스 | 처리 방식 | 코드 위치 |
|--------|-----------|-----------|
| 발행 주식 매수 | 발행가 이하만 체결, 잔량 차감 | L.77~97 |
| 학생 간 부분 체결 | `OrderMatcher.match()` → `MatchResult` 반환 | L.100~140 |
| 미체결 수량 대기 등록 | `remainingAmount > 0` → `대기` 상태 주문 DB 저장 | L.145~159 |
| 전체 호가 브로드캐스트 | `convertAndSend("/topic/orders/{stockId}")` | L.53, L.93 |
| 개인 알림 | `convertAndSendToUser(studentId, "/queue/notifications")` | L.57, L.94 |

### 3-2. 호가 단위 검증 (`StockOrderServiceImpl.java` L.44~50)

```java
private int getTickSize(int price) {
    if (price < 1000)  return 1;
    if (price < 5000)  return 5;
    if (price < 10000) return 10;
    if (price < 50000) return 50;
    return 100;
}
```

실제 한국 주식 시장의 호가 단위 규칙을 도메인 로직으로 구현.  
단순 CRUD가 아닌 **도메인 이해 기반 설계** 증거.

### 3-3. 예외 처리 체계 (`ErrorCode.java` + `GlobalExceptionHandler.java`)

```
StockGameException (커스텀 도메인 예외)
  └ InsufficientPointException   → 400 BAD_REQUEST
  └ InvalidTickSizeException      → 400 BAD_REQUEST
  └ MarketClosedException         → 400 BAD_REQUEST
  └ NotYourOrderException         → 403 FORBIDDEN
  └ OrderNotFoundException        → 404 NOT_FOUND
  └ ...
        ↓
@RestControllerAdvice (GlobalExceptionHandler)
  └ 전사 일관 ApiResponse<Void> 에러 응답
```

HTTP 상태코드 400/401/403/404/500 모두 도메인 의미를 담아 세분화.

### 3-4. 스케줄러 (`StockSchedulerService.java`)

```java
@Scheduled(cron = "0 0 0 * * *")
@Transactional
public void syncPreviousDayPrices() {
    stockPriceHistoryRepository.updatePrevPricesToLatestClose();
}
```

수동 개입 없이 매일 자정 전일 종가를 `stocks.prev_price`에 자동 갱신.

### 3-5. WebSocket 복원력 (`StockDetail.jsx` L.83~97)

```jsx
const { status: wsStatus, retryCount } = useStompResilience({
    url: 'http://localhost:8882/ws',
    subscriptions: [{ topic: `/topic/orders/${stockId}`, callback: ... }],
    maxReconnectAttempts: 5
});
```

- 재연결 최대 5회, 상태를 `CONNECTED / CONNECTING / RECONNECTING / FAILED / DISCONNECTED` 로 UI에 시각화
- `Promise.all()` 로 주식정보·히스토리·호가 3개 API 병렬 호출

### 3-6. 테스트 코드 (`StockOrderControllerTest.java`)

- `@WebMvcTest` 슬라이스 테스트 (전체 컨텍스트 불필요)
- `@AutoConfigureMockMvc(addFilters = false)` 보안 필터 분리
- `given(...).willReturn(...)` BDD 스타일
- `requestAttr("studentId", "student1")` 로 JWT 없이 인증 컨텍스트 주입

---

## 4. 채용 공고 × 포트폴리오 매칭 분석

> 11개 사람인 공고 분석 결과 기준 (Java/Spring 63%, Python AI 18%, C++ 9%, 퍼블리싱 9%)

| 공고 유형 | 요구 기술 | 프로젝트 내 근거 | 매칭 강도 |
|-----------|-----------|-----------------|:---------:|
| Java/Spring 백엔드 (63%) | Spring Boot, JPA, MyBatis, JWT | `StockOrderServiceImpl`, `Student.java`, `JwtFilter` | ★★★★★ |
| 실시간 처리 / WebSocket | STOMP, WebSocket | 브로드캐스트 + 개인 알림 이중 채널 구조 | ★★★★★ |
| 예외·응답 체계 | 공통 응답 포맷, 에러 핸들링 | `GlobalExceptionHandler` + `ErrorCode` + `ApiResponse` | ★★★★☆ |
| 배치·스케줄러 | @Scheduled, 자동화 | `StockSchedulerService` | ★★★★☆ |
| 프론트엔드 협업 | React, REST API 설계 | `stockGame_react`, CORS 처리 | ★★★☆☆ |
| 테스트 코드 | JUnit, MockMvc | `StockOrderControllerTest` | ★★★★☆ |

---

## 5. 포트폴리오 3대 핵심 서사

### 서사 1: "배움을 코드로 증명한 개발자"
```
노션 학습 커리큘럼 → stockGame (Servlet 직접 구현)
→ stockGame_spring (Spring 전환) → stockGame_react (풀스택)
```
강의 수강에 그치지 않고 **매 단계를 실제 동작하는 시스템으로 구현한 이력** 존재.

### 서사 2: "금융 도메인을 이해하는 개발자"
호가 단위, 부분 체결, 발행 주식 vs 학생 간 거래 분리, 포인트 정산까지  
실제 증권 시스템의 **복잡한 비즈니스 규칙을 코드 레벨에서 직접 설계**함.

### 서사 3: "운영을 고려하는 개발자"
- **장애 대비:** WebSocket 재연결 복원력 (useStompResilience, 최대 5회)
- **자동화:** @Scheduled 전일 종가 동기화 배치
- **일관성:** 도메인 예외 → HTTP 응답 전사 통일
- **검증:** @WebMvcTest 슬라이스 테스트 작성

---

## 6. 발견한 개선 필요 사항 (포트폴리오 작성 시 주의)

| 항목 | 현재 상태 | 포트폴리오 대응 |
|------|-----------|----------------|
| 보유 주식 API 미완 | `StockDetail.jsx` L.251에 `'보유 주식 (서버에서 가져와야함)'` 주석 잔존 | 완성된 기능 위주로만 서술, 미완 기능 언급 X |
| 동시성 처리 | DB Lock / Redis 미적용 | "동시성 이슈를 인지하고 개선 과제로 설정"으로 작성 |
| 배포 경험 | 로컬 개발 환경 위주 | 리드커리어 서비스 운영 경력과 연결 |
| JPA vs MyBatis 혼용 | `Student.java`는 JPA, 대부분은 MyBatis XML | 두 기술 모두 경험했음을 강점으로 서술 |

---

## 7. 기술 스택 최종 정리

```
Backend
  Spring Boot 3.x
  Spring Security + JWT (Stateless)
  Spring Data JPA (엔티티 관리)
  MyBatis XML Mapper (복잡 쿼리)
  Spring WebSocket + STOMP
  Spring @Scheduled (배치)
  @RestControllerAdvice (예외 처리)
  JUnit 5 + MockMvc (테스트)

Frontend
  React + Vite
  Zustand (전역 인증 상태)
  STOMP.js + 커스텀 useStompResilience 훅
  ReactApexChart (캔들스틱 차트)
  React Router v6

Database
  MySQL
  MyBatis XML Mapper
  JPA @Entity

Architecture
  레이어드 아키텍처 (Controller → Service → Repository)
  REST API
  WebSocket 실시간 통신
  도메인 예외 계층 구조
```
