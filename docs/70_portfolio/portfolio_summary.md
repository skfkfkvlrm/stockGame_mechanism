# 📈 STKGAME — 모의 주식 거래 시스템 포트폴리오

> **학생 대상 교육용 모의투자 플랫폼** | Java 21 · Spring Boot 3.5 · React 18 · MSA 아키텍처

---

## 1. 프로젝트 개요

학교 환경에서 학생들이 **가상 포인트**로 실제 주식 시장과 유사한 매매 경험을 쌓을 수 있도록 설계된 풀스택 모의 투자 시뮬레이션 플랫폼입니다.
초기에는 JSP 기반 모놀리식 구조로 시작하였으며, **총 7개 마이크로서비스(MSA)로 전환 완료**하였습니다.

---

## 2. 기술 스택 전체 정리

| 분류 | 기술 |
|---|---|
| **Language** | Java 21 |
| **Backend Framework** | Spring Boot 3.5.x |
| **Architecture** | Spring Cloud MSA (Eureka + API Gateway) |
| **Database** | MariaDB |
| **ORM / SQL Mapper** | Spring Data JPA + MyBatis 하이브리드 |
| **Security** | Spring Security + JWT + BCrypt |
| **Real-time** | WebSocket (STOMP / SockJS) |
| **Frontend** | React 18 + Vite + React Router v6 |
| **State Management** | Zustand |
| **Chart** | ApexCharts (캔들스틱) |
| **Build** | Maven (Multi-Module) |

---

## 3. 아키텍처 진화 과정 (Evolution Timeline)

```
[Phase 1] 모놀리식 JSP + Spring MVC
          ↓ REST API 서버로 분리 (Track A)
[Phase 2] Spring Boot REST API 서버 (단일 서비스)
          + React SPA (별도 레포)
          ↓ 대규모 MSA 전환 (Track C)
[Phase 3] Spring Cloud MSA (7개 서비스)
          Eureka Server → API Gateway (8000)
          ├── member-service  (8081)
          ├── stock-service   (8082)
          ├── point-service   (8083)
          ├── coupon-service  (8084)
          └── ai-news-service (별도)
```

---

## 4. 포트폴리오 핵심 하이라이트

---

### 🔥 [1] 부분 체결 주식 매칭 엔진 (Partial Fill Order Matching Engine)

**가장 핵심적인 비즈니스 로직**이자 직접 설계하고 구현한 알고리즘입니다.

#### 구현 내용

- **3단계 주문 처리 파이프라인**:
  1. **IPO 매수**: 발행 잔량(`pub_amount`)이 남아있으면 발행가로 즉시 체결
  2. **P2P 매칭**: 반대 주문(매수↔매도) 호가가 일치하면 `SELECT FOR UPDATE` 비관적 잠금으로 Race Condition 방지 후 체결
  3. **대기 등록**: 매칭 실패 시 대기 큐(Order Book)에 등록, 이후 반대 주문 진입 시 재매칭

- **부분 체결(Split) 전략**: 수량이 맞지 않아도 체결 가능한 수량만큼 즉시 처리.
  대기 주문의 잔량을 UPDATE하고, 체결된 수량만큼 신규 트랜잭션 레코드를 INSERT하는 우회 전략으로 DB 정합성 유지.

- **발행 잔량 소진 후 유저 간 순수 거래량(tradeVolume) 분리 집계**:
  최초 IPO 물량은 거래량에서 제외하고 `stock_transactions` JOIN SUM 방식으로 실제 시장 체결 수량만 별도 관리.

#### 코드 포인트

```java
// StockOrderServiceImpl.java - 부분체결 루프
while (remainAmount > 0) {
    Order matchOrder = findBestMatchOrder(stockId, price, oppContent);
    if (matchOrder == null) break;
    int fillAmount = Math.min(remainAmount, matchOrder.getAmount());
    // ... 체결 처리, OHLCV 갱신, WebSocket 브로드캐스트
    remainAmount -= fillAmount;
}
```

---

### 🌐 [2] Spring Cloud MSA 전환 (7 Microservices)

단일 Spring Boot 서버를 **Eureka Service Discovery + API Gateway** 기반의 7개 마이크로서비스로 전환했습니다.

#### 서비스 구성

| 서비스 | 역할 | 포트 |
|---|---|---|
| `eureka-server` | 서비스 등록 및 디스커버리 | 8761 |
| `gateway-service` | 라우팅 + JWT 인증 필터 | 8000 |
| `member-service` | 학생 인증, 계정 관리, 회원가입 | 8081 |
| `stock-service` | 주식 목록, 상세, 주문 매칭, OHLCV | 8082 |
| `point-service` | 포인트 내역, 자산 대시보드 | 8083 |
| `coupon-service` | 쿠폰 상점, 쿠폰 구매/보유 관리 | 8084 |
| `ai-news-service` | AI 기반 시장 뉴스 자동 생성 | - |

#### 핵심 설계 결정

- **Gateway에서 JWT 인증**: 각 서비스는 인증 로직을 갖지 않고, Gateway에서 단일 검증 후 `X-Student-Id` 헤더로 내려전달.
- **하이브리드 데이터 접근**: JPA(DDL 자동 생성 전용) + MyBatis(실제 비즈니스 쿼리) 병용으로 유연성과 성능 양립.

---

### 🔒 [3] 하이브리드 보안 체계 (Dual Auth System)

교사(관리자)와 학생이라는 두 가지 다른 인증 요구사항을 하나의 Spring Security 설정으로 처리했습니다.

- **이중 필터 체인(Dual Filter Chain)**:
  - **Chain 1** (`@Order(1)`): `/admin/**` 전용 — Form Login + JPA `AppUser` 엔티티 기반 관리자 인증
  - **Chain 2** (`@Order(2)`): 그 외 학생 경로 — JWT 토큰 + HttpSession 병행 인증

- **보안 강화 이력**:
  - 초기 평문 비밀번호 저장 → **BCrypt 단방향 해싱** 전환
  - **무중단 마이그레이션**: 기존 평문 사용자가 로그인 성공 시 백그라운드에서 자동 BCrypt 암호화 업데이트 적용 (Zero Downtime Migration)
  - **세션 위조 취약점 패치**: `@SessionAttribute(required=false)` 도입으로 비로그인 주문 접수 취약점 차단

---

### ⚡ [4] WebSocket(STOMP) 실시간 호가창 및 알림

주문이 접수되거나 체결될 때마다 실시간으로 UI를 갱신하는 양방향 통신을 구현했습니다.

- **호가창 브로드캐스트** (`/topic/orders/{stockId}`): 전체 사용자에게 실시간 주문북 갱신
- **개인 체결 알림** (`/queue/notifications`): 체결 당사자에게만 개인 푸시 알림
- **`useStompResilience` 커스텀 훅**: React 프론트에서 WebSocket 연결 끊김 자동 재연결 및 연결 상태 뱃지 시각화

---

### 📊 [5] OHLCV 자동 기록 + 스케줄러 + ApexCharts 캔들스틱

금융 데이터의 핵심인 OHLCV(시가·고가·저가·종가·거래량) 기록 시스템을 설계했습니다.

- **실시간 갱신**: 매칭 엔진 체결 시점에 `ON DUPLICATE KEY UPDATE`(Upsert)로 당일 고가/저가 및 누적 거래량 즉시 반영
- **자동 기준가 갱신**: `@Scheduled(cron = "0 0 0 * * *")` — 매일 자정 전날 종가를 `prev_price`로 자동 동기화
- **프론트 차트**: React `ApexCharts`로 종목 상세 페이지에 대화형 캔들스틱 차트 렌더링

---

### 🤖 [6] AI 뉴스 자동 생성 서비스 (ai-news-service)

현재 등록된 주식 종목 목록을 기반으로 AI가 시장 뉴스를 자동 생성하는 마이크로서비스입니다.

- 단순 하드코딩 뉴스가 아닌, 실제 등록된 종목명을 기반으로 맥락 있는 시장 뉴스를 동적으로 생성
- 별도 서비스로 분리하여 뉴스 생성 로직을 독립 배포/확장 가능하도록 설계

---

### 🖥️ [7] React SPA 프론트엔드 (Feature-Sliced Design)

React 18 + Vite + Zustand로 구성한 SPA 프론트엔드를 `features/` 단위 도메인 구조로 설계했습니다.

#### 화면별 구현 내역

| Feature | 주요 기능 |
|---|---|
| `auth` | 로그인, 회원가입, 아이디 중복확인 (JWT 저장) |
| `dashboard` | 총 자산, 보유 포인트, 수익률, 보유 주식 현황 |
| `stocks` | 종목 목록 (분야별 다중 필터, 거래량·등락률 정렬), 캔들스틱 차트, 매수/매도 폼, 실시간 호가창 |
| `ranking` | 실시간 학생 총자산 리더보드 |
| `coupons` | 쿠폰 상점, 쿠폰 구매, 내 쿠폰함 |
| `points` | 포인트 입출금 이력 조회 |
| `news` | AI 생성 시장 뉴스 피드 |
| `admin` | 학생 관리(추가/삭제/포인트 조정), 주식 CRUD, 시장 개/폐장 토글, 관리자 인증 |

#### 핵심 UI/UX 구현

- **분야별 다중 선택 필터**: 7개 분야 모두 선택 시 자동으로 '전체'로 수렴하는 UX 로직
- **고정 헤더 스크롤 테이블**: `position: sticky; z-index: 20;` + 불투명 배경으로 스크롤 시 컬럼 헤더 고정
- **관리자 패널**: 신규 학생 등록, 계정 삭제(`confirm` 안전 팝업), 포인트 지급/차감 모달 완비

---

### 📱 [8] www & m 멀티 도메인 반응형 아키텍처 설계

`www.stkgame.com`(데스크톱)과 `m.stkgame.com`(모바일) 두 도메인을 **단일 React SPA**로 처리하는 반응형 전략을 수립하고 기획서로 문서화했습니다.

| 디바이스 | 브레이크포인트 | 네비게이션 | 주식 목록 |
|---|---|---|---|
| 스마트폰 | 640px 이하 | 하단 탭바 (Bottom Nav) | 카드 뷰 |
| 태블릿 | 641~1024px | 슬림 사이드바 (Nav Rail) | 테이블 (밀도 조정) |
| 데스크톱 | 1024px 초과 | 완전 확장 사이드바 | 고정 헤더 스크롤 테이블 |

---

## 5. 프로젝트 진행 과정 타임라인

```
2026-07 초  ─ JSP 모놀리식 + 치명적 버그 수정 (주문 취소 환불 불가, 세션 위조 취약점)
           ─ Spring Security 이중 체인 + BCrypt 도입
           ─ 부분 체결 엔진 완성 (가장 중요한 마일스톤)
           ─ REST API 서버 전환 + GlobalExceptionHandler 구축
           ─ OHLCV + 스케줄러 도입
           ─ WebSocket(STOMP) 실시간 호가창 연동

2026-07 중  ─ React SPA 마이그레이션 완료 (전체 화면 JSP → React)
           ─ Spring Cloud MSA 전환 (7개 서비스)
           ─ JWT Gateway 인증 파이프라인 구축
           ─ 실시간 랭킹 리더보드, 쿠폰 상점 완성

2026-07 말  ─ 관리자 패널 (학생 관리, 주식 CRUD, 시장 토글) 완성
           ─ AI 뉴스 서비스 연동

2026-08 초  ─ 주식 분야별 필터 다중 선택 UX
           ─ 발행 잔량 분리 순수 유저 간 거래량 집계
           ─ 관리자 학생 추가/삭제 기능
           ─ 스크롤 테이블 고정 헤더
           ─ www/m 반응형 아키텍처 기획
```

---

## 6. 특기할 기술적 도전과 해결

| 도전 | 해결 방법 |
|---|---|
| 부분 체결 시 DB 정합성 유지 | 잔량 UPDATE + 체결량 INSERT 분리 (Split 전략) + `@Transactional` |
| Race Condition (동시 주문) | `SELECT FOR UPDATE` 비관적 잠금 |
| 기존 평문 비밀번호 사용자 마이그레이션 | 로그인 성공 시점 백그라운드 BCrypt 자동 업데이트 |
| JSP → React API 서버 전환 | `ApiResponse<T>` 공통 포맷 + `GlobalExceptionHandler` |
| MSA 서비스 간 인증 공유 | Gateway JWT 검증 → `X-Student-Id` 헤더 하방 전달 |
| WebSocket 연결 불안정 | `useStompResilience` 커스텀 훅 자동 재연결 |
| 거래량 = IPO 물량 포함 문제 | `stock_transactions` 테이블 JOIN SUM으로 순수 P2P 체결량만 집계 |

---

## 7. 리포지토리 링크

- **백엔드 (MSA)**: https://github.com/skfkfkvlrm/stockGame_msa
- **프론트엔드**: https://github.com/skfkfkvlrm/stockGame_react
- **레거시 단일 서버**: https://github.com/skfkfkvlrm/stockGame_spring
