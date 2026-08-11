# 📈 STKGAME — 모의 주식 거래 시스템 포트폴리오

> **학생 대상 교육용 모의투자 플랫폼** | Java 21 · Spring Boot 3.5 · React 18 · MSA 아키텍처 · 개발자 천사무엘

---

## 1. 프로젝트 개요 & 개발자 소개

학교 환경에서 학생들이 **가상 포인트**로 실제 주식 시장과 유사한 매매 경험을 쌓을 수 있도록 설계된 풀스택 모의 투자 시뮬레이션 플랫폼입니다.

- **개발자**: 천사무엘 (Backend & System Architecture)
- **성장 및 전환 과정**: JSP 기반 레거시 모놀리식 웹 백엔드로 시작하여 **REST API 전환**, **Spring Security 이중 체인 도입**, **부분 체결 매칭 엔진 구축**, **React 18 SPA 디커플링**을 거쳐 **총 7개 Spring Cloud 마이크로서비스(MSA)**로 진화 완료하였습니다.
- **핵심 역량 연계**: 명확한 아키텍처 가이드라인에 기반한 정교한 트랜잭션 동시성 제어(`SELECT FOR UPDATE`), 영속성 데이터 정합성 유지, 그리고 회복성 높은 웹소켓 기반 실시간 통신망을 수립했습니다.

---

## 2. 기술 스택 전체 정리

| 분류 | 기술 스택 |
|---|---|
| **Language** | Java 21, JavaScript (ES6+) |
| **Backend Framework** | Spring Boot 3.5.x |
| **Architecture** | Spring Cloud MSA (Eureka Service Discovery + API Gateway 8000) |
| **Database & Cache** | MariaDB, Redis |
| **ORM / SQL Mapper** | Spring Data JPA + MyBatis 하이브리드 데이터 계층 |
| **Security & Auth** | Spring Security + JWT + BCrypt (Zero Downtime Auto-Migration) |
| **Real-time** | WebSocket (STOMP / SockJS), `useStompResilience` Custom Hook |
| **Frontend** | React 18 + Vite + Zustand + React Router v6 |
| **Chart & UI** | ApexCharts (캔들스틱), Tailwind / CSS Modules |
| **Build & Tooling** | Maven (Multi-Module), Git, Postman, Docker |

---

## 3. 아키텍처 진화 과정 (Evolution Timeline)

```
[Phase 1] Servlet/JSP 모놀리식 웹 (FrontController, 수동 세션, 레거시 DAO)
          ↓ 0단계 결함 진단 및 REST API 서버 전환 (Track A)
[Phase 2] Spring Boot 3.5 REST API 서버 (@RestController, JPA + MyBatis 하이브리드)
          + Spring Security (Dual Auth System: Form Login + JWT)
          ↓ 부분 체결 매칭 엔진 & WebSocket 실시간 호가/알림 도입 (Track B)
[Phase 3] React 18 SPA 디커플링 (Vite + Zustand, ApexCharts, Feature-Sliced)
          ↓ 대규모 Spring Cloud MSA 전환 (Track C)
[Phase 4] Spring Cloud MSA (7개 서비스)
          Eureka Server (8761) → API Gateway (8000 라우팅 및 JWT 단일 검증)
          ├── member-service  (8081) — 회원, 인증, 학생 계정 관리
          ├── stock-service   (8082) — 주식 목록, 매칭 엔진, OHLCV
          ├── point-service   (8083) — 포인트 이력, 자산 대시보드
          ├── coupon-service  (8084) — 쿠폰 상점, 보유 쿠폰 관리
          └── ai-news-service (독립) — AI 동적 시장 뉴스 생성
```

---

## 4. 포트폴리오 핵심 하이라이트 (8가지)

---

### 🔥 [1] 부분 체결 주식 매칭 엔진 (Partial Fill Order Matching Engine)

**가장 핵심적인 비즈니스 로직**이자 직접 설계하고 구현한 동시성 제어 매칭 알고리즘입니다.

- **3단계 주문 처리 파이프라인**:
  1. **IPO 매수**: 최초 발행 잔량(`pub_amount`)이 남아있으면 발행가로 즉시 체결.
  2. **P2P 비관적 잠금 매칭**: 반대 주문(매수↔매도) 호가 일치 시 `SELECT FOR UPDATE` 비관적 잠금으로 Race Condition 방지 후 체결.
  3. **대기 등록 & 재매칭**: 매칭 미완료 시 대기 큐(Order Book) 등록 후 차후 주문 진입 시 재매칭.
- **수량 Split 전략**: 잔량 수량이 일치하지 않아도 체결 가능한 수량만큼 즉시 체결 후, 대기 주문 잔량 UPDATE & 체결 수량 신규 INSERT 분리 우회 전략으로 DB 정합성 완벽 유지.
- **순수 P2P 거래량(tradeVolume) 분리 집계**: 최초 IPO 방출 물량을 제외하고 `stock_transactions` 테이블 JOIN SUM 방식으로 사용자 간 실제 체결 거래량만 정밀 분리 정렬.

---

### 🌐 [2] Spring Cloud MSA 전환 (7 Microservices)

단일 Spring Boot 서버를 **Eureka Service Discovery + API Gateway** 기반 7개 마이크로서비스로 분리 구축했습니다.

| 서비스 | 역할 | 포트 |
|---|---|---|
| `eureka-server` | 서비스 등록 및 디스커버리 센터 | 8761 |
| `gateway-service` | API 라우팅 + JWT 단일 검증 및 Header 전달 | 8000 |
| `member-service` | 학생/교사 계정 관리, 회원가입, JWT 인증 파이프라인 | 8081 |
| `stock-service` | 주식 종목, 부분 체결 매칭 엔진, OHLCV 차트 데이터 | 8082 |
| `point-service` | 포인트 입출금 내역, 대시보드 자산 현황 조회 | 8083 |
| `coupon-service` | 쿠폰 상점, 쿠폰 구매 및 내 쿠폰함(/my-coupons) | 8084 |
| `ai-news-service` | AI 기반 가상 시장 뉴스 자동 생성 | 독립 서비스 |

- **Gateway 중앙 인증**: 각 마이크로서비스가 독립 인증 코드를 갖지 않고 Gateway에서 JWT 검증 완료 후 `X-Student-Id` 헤더로 Downstream 서비스 전달.

---

### 🔒 [3] 하이브리드 보안 체계 (Dual Auth System) & 무중단 BCrypt 마이그레이션

교사(관리자)와 학생의 서로 다른 인증 정책을 단일 보안 파이프라인으로 구현했습니다.

- **이중 필터 체인(Dual Filter Chain)**:
  - **Chain 1 (`@Order(1)`)**: `/admin/**` 전용 — Form Login + JPA `AppUser` 관리자 권한 인증.
  - **Chain 2 (`@Order(2)`)**: 학생 전용 — JWT 토큰 + Session 병행 검증 체인.
- **무중단 BCrypt 마이그레이션**: 초기 평문 비밀번호 유저가 로그인 성공하는 시점에 백그라운드에서 자동 BCrypt 단방향 암호화 업데이트 적용 (Zero Downtime Migration).
- **세션 위조 취약점 차단**: `@SessionAttribute(required=false)` 명시 및 JWT 토큰 파싱 검증으로 비인증 접근 완전 차단.

---

### ⚡ [4] WebSocket(STOMP) 실시간 호가창 & 회복성 훅

- **실시간 호가 브로드캐스트**: `/topic/orders/{stockId}` 채널을 통해 호가창 및 주문북 전체 유저 실시간 동기화.
- **개인 체결 알림**: `/queue/notifications` 채널로 체결 당사자에게만 타겟팅 푸시 알림 발송.
- **`useStompResilience` 커스텀 훅**: React 프론트엔드에서 네트워크 끊김 시 지연 자동 재연결 및 실시간 STOMP 상태 뱃지 시각화.

---

### 📊 [5] OHLCV 자동 기록 + 스케줄러 + ApexCharts 캔들스틱

- **실시간 OHLCV Upsert**: 매칭 엔진 체결 시 `ON DUPLICATE KEY UPDATE` 기반 당일 고가/저가/종가 및 거래량 즉시 집계.
- **자정 기준가 동기화**: `@Scheduled(cron = "0 0 0 * * *")` 스케줄러로 매일 자정 전날 종가를 `prev_price`로 자동 이관.
- **대화형 차트 UI**: React `ApexCharts` 연동으로 주식 상세 페이지에 실시간 캔들스틱 차트 구현.

---

### 🤖 [6] AI 뉴스 자동 생성 서비스 (ai-news-service)

- 등록 주식 종목 리스트를 기반으로 AI가 시장 맥락을 분석하여 가상 뉴스를 동적 생성하는 독립 마이크로서비스 연동.

---

### 🖥️ [7] React 18 SPA 프론트엔드 (Feature-Sliced Design)

- **Vite + Zustand** 기반 9개 Feature(`auth`, `dashboard`, `stocks`, `ranking`, `coupons`, `points`, `news`, `admin`) 도메인별 분리.
- **분야별 다중 선택 필터 UX**: 7개 분야 다중 선택 지원 및 전체 선택 시 자동으로 '전체' 버튼 수렴 UX 구현.
- **sticky 헤더 고정 테이블**: `position: sticky; z-index: 20;` 및 불투명 배경 적용으로 스크롤 시 테이블 헤더 비침 차단.
- **관리자 전용 학생 관리 패널**: 신규 학생 등록 모달(기본 10만 P 시드머니), 학생 계정 삭제 API 연동 및 `confirm` 안전 팝업 구축.

---

### 📱 [8] www & m 멀티 도메인 반응형 웹 아키텍처 설계

- `www.stkgame.com`(데스크톱)과 `m.stkgame.com`(모바일)을 단일 React SPA로 처리하는 반응형 아키텍처 수립 및 문서화.
- 3단계 브레이크포인트(640px 이하 모바일 하단 탭바/카드뷰 ↔ 1024px 초과 데스크톱 사이드바/고정 테이블) 완비.

---

## 5. 특기할 기술적 도전과 해결 (Problem-Solving Matrix)

| 기술적 문제 / 도전 | 근본 원인 및 분석 | 혁신적 해결 방법 |
|---|---|---|
| **부분 체결 시 DB 정합성 유휴** | 주문 수량이 불일치할 때 단일 UPDATE 시 데이터 유실 위험 | 잔량 UPDATE + 체결량 INSERT 분리 우회 전략 (`Split Fill`) 및 `@Transactional` 원자성 보장 |
| **동시 주문 시 Race Condition** | 동일 종목에 동시 매수/매도 진입 시 잔량 충돌 | `SELECT FOR UPDATE` 비관적 잠금(Pessimistic Lock) 적용 |
| **기존 평문 유저 암호화 마이그레이션** | 기존 데이터베이스에 평문 비밀번호가 잔존 | 로그인 성공 시점 백그라운드 자동 BCrypt 해싱 업그레이드 (Zero Downtime) |
| **JSP → React 전환 시 API 응답 불일치** | HTML 반환 컨트롤러와 JSON API 반환 구조 혼재 | `ApiResponse<T>` 통일 포맷 + `@RestControllerAdvice` 커스텀 예외 6종 구축 |
| **MSA 서비스 간 인증 중복** | 마이크로서비스마다 독립 JWT 파싱 시 코드 중복 및 성능 저하 | Gateway 단일 인증 처리 → Downstream 서비스로 `X-Student-Id` 헤더 하방 전달 |
| **WebSocket 통신 불안정** | 네트워크 소실 시 소켓 연결 단절 | `useStompResilience` 커스텀 훅 자동 재연결 및 UI 연결 상태 뱃지 표시 |
| **거래량 정렬 시 IPO 물량 포함 착시** | 최초 주식 생성 시 IPO 방출 수량이 거래량에 더해짐 | `stock_transactions` 테이블 JOIN SUM으로 순수 P2P 체결량만 분리 집계 |

---

## 6. 리포지토리 & 블로그 링크

- **백엔드 (MSA)**: [https://github.com/skfkfkvlrm/stockGame_msa](https://github.com/skfkfkvlrm/stockGame_msa)
- **프론트엔드 (React)**: [https://github.com/skfkfkvlrm/stockGame_react](https://github.com/skfkfkvlrm/stockGame_react)
- **개발자 Velog 블로그**: [https://velog.io/@skfkfkvlrm/posts](https://velog.io/@skfkfkvlrm/posts)
- **통합 Spring 레포지토리**: [https://github.com/goodjwon/day_by_spring_sm](https://github.com/goodjwon/day_by_spring_sm)
