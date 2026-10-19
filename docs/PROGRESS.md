# 백엔드 프로젝트 진행 상황 (stockGame_spring)

## 현재 상태: 트랙 A (백엔드 개선) 100% 완료 🎉

현재 `stockGame_spring` 프로젝트는 기존의 레거시 JSP 기반 모놀리식 구조에서 탈피하여, 프론트엔드와 분리된 완벽한 **REST API 서버**이자 실시간 통신을 지원하는 **주식 게임 엔진**으로 고도화되었습니다.

### 🚀 완료된 핵심 기능 (1단계 ~ 6단계)
1. **보안 및 아키텍처 개편 (1단계, 3단계)**
   - Spring Security + JPA를 도입하여 교사(관리자)와 학생의 인증 경로 분리 완료
   - BCrypt 암호화, 글로벌 예외 처리(`@RestControllerAdvice`) 및 `ApiResponse<T>` 기반 통합 응답 포맷 구축
2. **거래 엔진 고도화 (2단계, 5단계)**
   - 스플릿(Split) 전략을 사용한 **주식 부분 체결 엔진** 도입
   - `ON DUPLICATE KEY UPDATE`를 활용한 실시간 OHLCV(고가/저가/거래량) 기록 누적 시스템 연동
   - 매일 자정 기준가(`prev_price`)를 갱신하는 자동 스케줄러 도입
3. **사용자 경험 및 시장 운영 정책 (6단계)**
   - `spring-boot-starter-websocket` 기반 STOMP 브로커 설정
   - 호가 단위(Tick Size) 및 시장 개/폐장(MarketSettings) 정책 적용 로직
   - 체결 완료 시 개인 알림(`/queue/notifications`) 및 호가창 브로드캐스트(`/topic/orders`) 발송

### 🔥 최신 스프린트 진행 상황 (완료)
- `[x]` `application.yaml`의 `ddl-auto` 및 `generate-ddl` 속성 비활성화 완료 (데이터 유실 방지)
- `[x]` 프론트엔드 `vite.config.js` 프록시 설정 하드코딩 제거 (`.env` 동적 할당)
- `[x]` `StockDetail.jsx` WebSocket 연결 누수(cleanup) 해결 (기반영됨 확인 완료)
- `[x]` `Transaction` 엔티티 `amount`, `price` 필드 추가 및 DB 동기화 (기반영됨 확인 완료)

### 📌 향후 계획
- 백엔드 자체적인 메인 로드맵은 모두 종료되었습니다.
- 최근 발견된 잔여 결함(트랜잭션 기록 무결성 및 ddl-auto 문제)에 대한 패치 및 컴파일 검증을 성공적으로 마쳤습니다 (참고: `backend_phase7_cleanup.md`, `backend_phase7_test_result.md`).
- 향후 프론트엔드(`stockGame_react`) 개발이 진행됨에 따라 추가적으로 필요한 데이터 필드나 API 수정 요청이 발생할 경우 대응하는 유지보수 형태로 진행될 예정입니다.

---

## 프론트엔드 React 마이그레이션 트랙 (별도 트랙) ✅ 완료

`stockGame_react` 프로젝트 (`feature/react-migration` 브랜치)를 통해 기존 JSP 화면을 React SPA로 100% 이관 완료.

### ✅ 완료된 작업
1. **공통 레이아웃** — `MainLayout`, `Sidebar` (유저 정보, 포인트, 메뉴 내비게이션)
2. **인증** — `Login.jsx`, `Register.jsx` (아이디 중복체크 포함)
3. **대시보드** — `Dashboard.jsx` (`/api/asset` 연동, 내 자산 현황)
4. **주식** — `StockList.jsx`, `StockDetail.jsx` (ApexCharts 캔들스틱 차트, 매수/매도 폼, WebSocket 실시간 연동)
5. **뉴스** — `NewsList.jsx` (`/api/news` 연동, 문자열 목록 렌더링 수정)
6. **포인트 내역** — `PointsHistory.jsx` (`/api/history` 연동)
7. **쿠폰 상점** — `CouponStore.jsx` (`/api/coupons` 연동 및 렌더링 안정화)
8. **더미 데이터** — `DataInitializer`로 초기 시드 데이터 (학생, 주식, 뉴스, 문폰) 자동 삽입

### ✅ 완료된 작업 (추가 버그 픽스 및 UI 고도화)
1. **주식 목록 데이터 500 오류 해결**: `Transaction` 엔티티 매핑 문제(`transactions` -> `stock_transactions`) 수정 및 쿼리 복구 완료
2. **리스트 중복 출력 버그 해결**: `DataInitializer`에 `INSERT IGNORE` 및 `COUNT(*)` 검사 추가, DB 중복 내역 청소 완료
3. **포인트 내역 화면 크래시 해결**: `PointsHistory.jsx` 데이터 빈 값 반환 시 옵셔널 체이닝 보호 구문 추가
4. **대시보드 에셋 페이지 UI 리워크**: 라벨 변경(총 자산, 보유 포인트, 총 손익, 보유 쿠폰) 및 클릭 이벤트 연동
5. **보유 쿠폰(My Coupons) 기능 신설**: 프론트엔드 `MyCoupons.jsx` 화면 개발 및 백엔드 `/api/coupons/my` 엔드포인트 구현 완료
6. **주식 상세 페이지 UI 및 로직 복구**: API 필드명 불일치(`currentPrice` -> `nowPrice`) 해결 및 React Uncontrolled input 경고 수정
7. **프론트 내비게이션 복구**: 주식 목록(`StockList`)의 각 행(Row) 클릭 시 `useNavigate`를 통한 상세 페이지 진입 기능 완비
8. **백엔드 쿠폰 및 거래 엔티티 구조 개선**: `CouponPurchase` 등 핵심 엔티티 누락 매핑(학생ID, 쿠폰ID 등) 복구 완료

### 🔗 관련 리포지토리
- 백엔드: [stockGame_spring](https://github.com/skfkfkvlrm/stockGame_spring)
- 프론트엔드: [stockGame_react](https://github.com/skfkfkvlrm/stockGame_react)
