# StockGame 프로젝트 통합 분석 및 현황 보고서 (Project Analysis)

## 1. 프로젝트 개요 (Overview)
*   **프로젝트명**: StockGame (가상 모의투자 및 학생 보상 관리 시스템)
*   **목적**: 학생들이 모의 주식 투자를 경험하고, 획득한 포인트로 학교생활 관련 혜택 쿠폰을 구매할 수 있는 교육용/보상용 통합 웹 애플리케이션.
*   **기술 스택**: 
    *   **Backend**: Spring Boot 3, MyBatis, MariaDB (Docker)
    *   **Frontend**: React 19, Vite, Zustand, Axios, Lucide React, ApexCharts

---

## 2. 개발 진행 현황 (Phases Completed)

### [Phase 1] Backend DDD 구조 리팩토링 완료
*   **진행 내용**: 기존의 거대한 기능별 패키지 구조를 **도메인 주도 설계(Domain-Driven Design)** 기반으로 완전히 개편.
*   **주요 도메인 분리**: 
    *   `auth`: 학생 인증(가입/로그인/로그아웃) 및 세션/권한 관리.
    *   `stock`: 전체 주식 목록, 단일 주식 조회, 매수/매도 주문 처리, 가격 히스토리.
    *   `asset`: 학생의 총 자산 현황 (보유 포인트 + 평가 금액) 및 포트폴리오.
    *   `history`: 포인트 증감 히스토리.
    *   `coupon`: 쿠폰 상점 목록, 구매, 보유 쿠폰 및 사용 승인 로직.
    *   `ai`: DeepSeek 등 로컬 AI와의 연동을 통한 투자 조언 기능(향후 확장).
*   **해결된 문제**: Spring Boot 서버 재구동 시 발생한 `MyBatis MapperScan` 패키지 경로 오류를 수정하여, DDD 도메인 패키지 하위의 Repository 파일들을 정상적으로 인식하도록 조치.

### [Phase 2] Frontend UI/UX 구현 (Light Theme + Glassmorphism)
*   **진행 내용**: 사용자 지침에 따라 다크 테마를 전면 배제하고, 화사한 **Light Theme 기반의 글래스모피즘(Glassmorphism)** 디자인 시스템 적용.
*   **주요 구현 사항**:
    *   `index.css`에 전역 CSS 변수를 설정하여 반투명 블러 패널(`backdrop-filter`), 은은한 그림자 톤을 통일감 있게 적용.
    *   대시보드(`Dashboard.jsx`), 주식 거래(`StockList.jsx`, `StockDetail.jsx`), 포인트 내역(`PointsHistory.jsx`), 쿠폰 상점(`CouponStore.jsx`, `MyCoupons.jsx`) 등 전체 페이지 레이아웃 구현 및 라우팅 설정.

### [Phase 3] Backend-Frontend API 연동 (Data Binding)
*   **진행 내용**: 프론트엔드의 하드코딩된 Mock 데이터를 모두 제거하고, Spring Boot 백엔드와 실시간 API 통신이 가능하도록 연결.
*   **주요 구현 사항**:
    *   `axios` 인스턴스 구성(`withCredentials: true`)을 통해 쿠키(세션) 기반 인증 유지.
    *   `vite.config.js` 프록시를 통해 `localhost:8080` (Spring Boot) 서버로 요청 우회 설정.
    *   로컬 모델(DeepSeek-R1)의 피드백을 반영하여 React 컴포넌트 렌더링 성능 최적화(정적 데이터 분리) 및 예외 처리(빈 값 입력 시 방어 코드) 적용.
    *   **테스트 대기 상태**: 회원가입, 로그인, 주식 매수/매도, 대시보드 업데이트 등 주요 트랜잭션 정상 호출되도록 프론트엔드 작업 완료.

---

## 3. 남은 작업 및 향후 계획 (Next Steps)
1.  **AI 조언 기능(AI Advisor) 통합**:
    *   학생들이 포트폴리오 화면에서 로컬 모델(DeepSeek-R1, Llama 3.2 등)에게 조언을 구할 수 있도록, 백엔드의 `AiAdvisorController` 및 프론트엔드 UI 연결 필요.
2.  **뉴스 모의 데이터(News API) 확장**:
    *   현재 `NewsList.jsx`는 Mock 데이터로 남아 있으나, 향후 백엔드 크롤링 혹은 스케줄러를 통해 시뮬레이션 뉴스 이벤트를 발행하는 기능 추가 필요.
3.  **선생님(Admin) 대시보드 기획**:
    *   학생들이 요청한 쿠폰 사용 승인, 전체 경제(인플레이션 등) 파라미터 조작, 주식 장 개/폐장 컨트롤을 위한 Admin 페이지 개발.
4.  **보안 및 세션 만료 예외 처리**:
    *   로그인 세션이 만료된 경우(401 Unauthorized), 인터셉터를 통해 자동으로 로그인 페이지로 라우팅되도록 `App.jsx`와 `useAuthStore`의 동기화 고도화.

---
> **보고서 작성일**: 2026-07-09
> **최신화 주체**: Agent (Antigravity & Local Models)
