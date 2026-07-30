# 📈 Stock Game Spring (Backend API Server)

학생 대상 주식 모의투자 시뮬레이션을 위한 **핵심 매칭 엔진 및 REST API 백엔드 서버**입니다.  
기존의 JSP 모놀리식 구조에서 탈피하여 React 프론트엔드와 독립적으로 통신하는 순수 API 서버이자 실시간 웹소켓 서버로 고도화되었습니다.

---

## 🚀 주요 기능 (Key Features)

### 1. 고성능 주식 매칭 엔진 (부분 체결 로직)
- 매수/매도 수량이 일치하지 않더라도 체결 가능한 수량만큼 즉시 체결되는 **Split(분할) 기반 부분 체결 시스템** 도입
- 트랜잭션 체결 수량(amount) 및 단가(price)를 DB에 1:1로 기록하여 트랜잭션 무결성을 완벽하게 보장하며, 복수의 대기 주문을 순회하며 일괄 체결 처리

### 2. 실시간 웹소켓 (STOMP) 연동
- `spring-boot-starter-websocket` 기반 양방향 실시간 통신
- **호가창 브로드캐스트 (`/topic/orders/{stockId}`)**: 주문이 접수되거나 체결될 때마다 전체 사용자에게 실시간 갱신 이벤트 전송
- **개인별 알림 (`/queue/notifications`)**: 주문이 체결되었을 때 당사자에게 즉시 푸시 알림 전송

### 3. 실시간 OHLCV 및 스케줄러 로직
- 거래 성사 시 `ON DUPLICATE KEY UPDATE`를 활용해 일일 **시가, 고가, 저가, 종가, 거래량** 실시간 갱신
- 매일 자정에 동작하는 **Spring Scheduler**를 통해 기준가(`prev_price`) 자동 동기화

### 4. 하이브리드 보안 & 데이터 접근 계층
- **관리자/교사**: Spring Security 및 JPA를 통한 엄격한 보안 통제 및 권한(Role) 관리  
  - JWT 인증 시 `ROLE_ADMIN` / `ROLE_MANAGER` / `ROLE_STUDENT` 정확히 반환
- **학생**: 경량화된 Session 기반 인증과 고성능 MyBatis 트랜잭션 혼용
- 비밀번호 암호화(BCrypt) 및 글로벌 예외 처리(`@RestControllerAdvice`)를 통한 API 안정성 및 응답 구조화(`ApiResponse<T>`) 완료

### 5. 관리자 전용 Admin API (신규)
| 엔드포인트 | 설명 |
|---|---|
| `GET /api/admin/students` | 학생 전체 목록 + 포인트 합계 조회 |
| `POST /api/admin/students/{id}/point` | 학생 포인트 지급/차감 + 이력 기록 |
| `GET /api/admin/students/{id}/detail` | 학생 보유 자산 및 주식 포트폴리오 조회 |
| `POST /api/admin/stocks` | 신규 주식 종목 상장 |
| `PUT /api/admin/stocks/{stockId}` | 종목 발행가/발행잔량 수정 |
| `DELETE /api/admin/stocks/{stockId}` | 종목 상장폐지(삭제) |
| `POST /api/admin/market/toggle` | 시장 개장/휴장 원터치 토글 |

> **입력값 방어 검증:** 발행가 ≤ 0 또는 발행잔량 < 0 요청 시 `IllegalArgumentException` 발생 (이중 방어)

### 6. 시장 정책 통제 API
- 코스피 시장과 동일한 **가격대별 호가 단위(Tick Size) 강제 검증** 로직
- 교사(관리자)가 시장의 개장/폐장 상태를 실시간 토글할 수 있는 운영 API 제공

---

## 🛠️ 기술 스택 (Tech Stack)

| 분류 | 기술 |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.x |
| **Database** | MariaDB |
| **ORM / SQL Mapper** | Spring Data JPA + MyBatis 하이브리드 |
| **Security** | Spring Security (BCryptPasswordEncoder, JWT) |
| **Real-time** | WebSocket (STOMP, SockJS) |
| **Build Tool** | Maven |

---

## 📂 프로젝트 구조 (DDD Architecture)

```
src/main/java/com/skfkfkvlrm/stockgame_spring/
 ├── config/           # Security, WebSocket, DataInitializer 설정
 ├── auth/             # JWT 필터 및 인증 유틸리티
 ├── domain/
 │   ├── admin/        # 관리자 API (학생·주식·시장 관리) ★신규
 │   │   ├── AdminController.java
 │   │   ├── AdminService.java / AdminServiceImpl.java
 │   │   ├── PointAdjustmentRequest.java  ★신규
 │   │   └── StockRequest.java            ★신규
 │   ├── member/       # 학생 인증·조회·포인트 도메인
 │   ├── stock/        # 주식 목록·상세·주문 매칭 도메인
 │   ├── point/        # 포인트 내역 도메인
 │   ├── coupon/       # 쿠폰 도메인
 │   ├── news/         # 시장 뉴스 도메인
 │   ├── ai/           # AI 관련 도메인
 │   └── common/       # 공통 응답 객체
 └── exception/        # 커스텀 비즈니스 예외 클래스
```

---

## ⚙️ 실행 방법 (How to Run)

1. **DB 환경 구성**
   - MariaDB에 `stockgame` 스키마 생성
   - `src/main/resources/application.yaml`에서 DB 접근 정보(username/password) 설정

2. **애플리케이션 구동**
   ```bash
   # Windows
   ./mvnw.cmd spring-boot:run

   # Mac / Linux
   ./mvnw spring-boot:run
   ```

3. 서버는 기본적으로 `http://localhost:8882` 포트에서 동작합니다.
4. 프론트엔드는 분리된 [stockGame_react](https://github.com/skfkfkvlrm/stockGame_react) 프로젝트를 통해 `5173` 포트에서 실행하여 연동합니다.

---

## 📋 최근 변경 이력 (Changelog)

| 날짜 | 내용 |
|---|---|
| 2026-07-30 | 관리자 학생 관리 API (포인트 지급/차감, 포트폴리오 조회) 추가 |
| 2026-07-30 | 주식 종목 CRUD API (상장·수정·삭제) 및 음수 입력 방어 검증 추가 |
| 2026-07-30 | JWT 인증 시 `ROLE_ADMIN` 오판별 버그 수정 |
| 2026-07-26 | 로그인 Resilience 강화 및 JWT 토큰 반환 파이프라인 표준화 |