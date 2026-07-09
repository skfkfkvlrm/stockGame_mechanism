# 백엔드(Spring Boot) DDD 구조 개편 및 구현 계획

DeepSeek-R1, Llama 3.2, Qwen(Llama-Guard) 및 저(클라우드 AI)까지 총 4개의 AI 모델이 협업하여 도출한 백엔드 구조 개편 방안입니다. `Simplicity First`와 `DDD(도메인 주도 설계)` 원칙을 엄격하게 적용했습니다.

## 👥 멀티 모델 논의 결과 요약

*   **DeepSeek-R1 (추론 심화):** "현재 기술적 계층(Technical Layer)으로 분리된 패키지는 도메인 응집도를 떨어뜨립니다. `member`, `stock`, `point` 등 비즈니스 도메인 단위로 패키지를 묶어야(Vertical Slicing) 모듈 간 결합도를 낮출 수 있습니다."
*   **Llama 3.2 (효율성):** "오버엔지니어링을 경계해야 합니다. 불필요하게 세분화된 DTO 클래스들을 도메인 패키지 내부로 캡슐화하거나 Inner Class를 활용해 파일 개수를 줄이는 'Simplicity First' 원칙을 적용해야 합니다."
*   **Qwen-Security / Llama-Guard (안정성):** "TDD 원칙에 따라, 구현을 시작하기 전 반드시 `WebMvcTest` 기반의 실패하는 컨트롤러 테스트 코드를 먼저 작성하여 API 스펙을 고정해야 합니다."

---

## 🛠️ Proposed Changes (개편 상세 계획)

### 1. 도메인 단위 패키지 재배치 (Vertical Slicing)

기술적 계층(`controller`, `service`, `repository`) 중심의 구조를 비즈니스 도메인 중심 구조로 변경합니다.

#### [NEW] Member Domain (`src/main/java/com/skfkfkvlrm/stockgame_spring/domain/member/`)
- `MemberController.java`
- `MemberService.java`
- `MemberRepository.java`
- `Member.java` (Entity)
- `MemberDto.java` 

#### [NEW] Stock Domain (`src/main/java/com/skfkfkvlrm/stockgame_spring/domain/stock/`)
- `StockController.java`
- `StockService.java`
- `StockRepository.java`
- `Stock.java` (Entity)
- `StockDto.java`

#### [NEW] Point Domain (`src/main/java/com/skfkfkvlrm/stockgame_spring/domain/point/`)
- `PointController.java`
- `PointService.java`
- `PointRepository.java`
- `Point.java` (Entity)

#### [DELETE] 기존 기술 계층 디렉토리 전체 삭제
- `src/main/java/com/skfkfkvlrm/stockgame_spring/controller/`
- `src/main/java/com/skfkfkvlrm/stockgame_spring/service/`
- `src/main/java/com/skfkfkvlrm/stockgame_spring/repository/`
- `src/main/java/com/skfkfkvlrm/stockgame_spring/domain/` (기존 패키지)

---

## ✅ Verification Plan (검증 및 TDD 계획)

### Automated Tests (TDD 진행)
1. **Controller Tests:** `WebMvcTest`를 활용하여 각 컨트롤러(Member, Stock, Point)의 엔드포인트 응답 상태를 검증하는 테스트 파일을 작성합니다.
2. **Refactoring:** 구조 변경 후 `gradlew test`를 실행하여 뼈대를 검증합니다.
