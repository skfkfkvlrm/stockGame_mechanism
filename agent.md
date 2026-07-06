# SchoolStock 프로젝트 에이전트 지침서 (agent.md)

> **CRITICAL RULE:** 과거에 학습한 지식에 의존하지 마십시오. 모든 작업 시 이 문서에 정의된 프로젝트 환경과 6단계 워크플로우를 최우선으로 준수해야 합니다.

## 1. 프로젝트 컨텍스트 (Project Context)
- **프로젝트명**: SchoolStock (학생 대상 주식 모의투자 시뮬레이션)
- **핵심 기술 스택**:
  - **백엔드 (`stockGame_spring`)**: Java 21, Spring Boot 3.5.x, MyBatis + JPA 혼용, Spring Security (이중 필터 체인), WebSocket (STOMP)
  - **프론트엔드 (`stockGame_react`)**: React 19, Vite 8, React Router v7, Axios, `@stomp/stompjs`
- **구조 및 네트워크**: 백엔드 포트 `8882`, 프론트엔드 포트 `5173`. 프론트엔드는 Vite Proxy를 통해 `/api`, `/ws` 요청을 백엔드로 전달합니다.

## 2. 필수 작업 프로세스 (Strict 6-Step Workflow)
모든 개발 및 수정 작업은 사소한 변경이더라도 반드시 다음 6단계를 거쳐야 합니다.
1. **계획 세우기 (Plan)**: 코드를 수정하기 전, 문제 원인을 분석하고 구체적인 계획서(`implementation_plan.md`)를 작성한 뒤 사용자 승인을 받습니다.
2. **체크리스트 작성 (Checklist)**: 승인된 계획을 바탕으로 `task.md`를 생성하여 작업 단위를 세분화합니다.
3. **작업 진행 (Execute)**: `task.md`를 따라 실제 코드 조작을 진행하며, 예상치 못한 이슈 발생 시 체크리스트를 갱신합니다.
4. **2차 확인 (Verify)**: 작업 후 코드가 정상적으로 빌드/실행되는지 반드시 테스트합니다. (예: `StockDetail.jsx` WebSocket 연결 해제 로직 정상 작동 여부 등)
5. **작업 내용 정리 (Document)**: 기능 구현이나 버그 픽스가 완료되면 `docs/` 폴더 내에 개별 문서(`작업명.md` 또는 `walkthrough.md`)로 작업 내역을 저장합니다.
6. **진행 상황 갱신 (Update Progress)**: 작업이 완전히 종료되면 프로젝트 루트의 `PROGRESS.md` 및 `README.md`를 최신 상태로 업데이트합니다.

## 3. 코드베이스 규칙 및 주의사항 (Architecture & Safety)
- **데이터 레이어 규칙**: 복잡한 조인/쿼리는 MyBatis(Mapper XML)를, 단순 CRUD 및 도메인 관리는 JPA를 사용하는 '하이브리드 구조'를 엄격히 따릅니다.
- **보안 및 인증**: 교사(관리자)는 Form 로그인, 학생은 세션 인증 기반의 이중 필터 체인을 사용 중입니다. 인증 로직 수정 시 두 체인이 간섭하지 않도록 주의합니다.
- **데이터베이스 보호**: 백엔드의 `ddl-auto: create` 설정은 DB 데이터 유실을 초래하므로 절대로 사용해선 안 됩니다. (반드시 `update` 또는 `none` 유지)

## 4. 스프린트 우선순위 인지 (Next Sprint Priorities)
에이전트는 작업 시 다음 우선순위 항목들을 인지하고 있어야 합니다:
1. `application.yaml`의 `ddl-auto` 속성 `none`으로 변경
2. `vite.config.js` 프록시 설정 하드코딩 제거
3. `StockDetail.jsx` WebSocket 연결 누수(cleanup) 해결
4. `Transaction` 엔티티 `amount`, `price` 필드 추가 및 DB 동기화

## 5. 에이전트 커뮤니케이션 원칙 (Agent Behavior)
- **Caveman Mode**: 불필요한 인사말, 변명, 부연 설명을 생략하고 핵심 코드와 필요한 단계만 즉시 제공하십시오.
- **Diagnose Pattern**: 에러가 발생했을 때 추측만으로 코드를 엎지 마십시오. **[에러 재현 ➡️ 가설 설정 ➡️ 확인]**의 정밀 진단 프로세스를 밟으십시오.
- **Self-Evolution**: 작업 중 지속적으로 발생하는 오류나 새롭게 알게 된 프로젝트 규칙이 있다면 이 문서(`agent.md`)에 규칙으로 추가할 것을 제안하십시오.
