# 등록 종목 전용 AI/실제 뉴스 자동 생성 엔진 구축 보고서

## 1. 개요
기존 뉴스 생성 엔진이 상장 종목(새콤달콤, PC방, SM 등)과 상관없는 외부 기업 뉴스(삼성전자, 현대차, LG에너지솔루션 등)를 더미로 생성하던 방식을 전면 개편하였습니다.  
현재 DB에 **상장(LISTED)되어 등록된 종목들만 타겟팅**하여 실시간 증시 뉴스 및 AI 찌라시/속보를 자동 생성하도록 `DynamicNewsService`를 구축했습니다.

---

## 2. 주요 개편 내역

1. **`DynamicNewsService.java` 신설 (`stock-service`)**:
   - `stockListRepository.getAllStocks()` 조회를 통해 등록된 종목 목록(`새콤달콤`, `PC방`, `SM` 등)을 동적으로 추출.
   - 외부 기업 키워드(삼성전자, SK하이닉스 등)를 전면 제거하고 오직 **등록된 종목명 및 현재가 정보**만 프롬프트 대상에 포함.

2. **이중 생성 알고리즘 (LLM + Fallback 템플릿)**:
   - **1차 (Ollama LLM)**: `qwen2.5-coder:7b` 모델을 활용하여 타겟 등록 종목에 관한 실제/과장 속보 기사를 1문장 자동 생성.
   - **2차 (Fallback 템플릿)**: Ollama 미구동/응답 지연 시 등록된 종목명을 바인딩한 증시 뉴스 템플릿으로 안전하게 생성.

3. **스케줄러 활성화 (`StockServiceApplication.java`)**:
   - `@EnableScheduling` 및 `@Scheduled(fixedRate = 300000)` 추가로 5분 간격 자동 적재.

---

## 3. 자가 검증 (E2E 테스트 결과)

- **`stock-service` 실행 후 DB 생성 확인 (`SELECT * FROM news ORDER BY news_id DESC LIMIT 1`)**:
  - `news_id: 189`
  - 생성된 내용: `[17:43:38] [속보] 오늘의 PC방은 대중문화의 중심이자 디지털 혁신의 선두를 이끄는 기업으로 알려져 있습니다.`
  - DB 등록 종목인 `PC방`에 특화된 실시간 속보가 정상 생성되어 적재되었음을 확인했습니다.
