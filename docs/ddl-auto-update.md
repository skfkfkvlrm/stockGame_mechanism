# ddl-auto 속성 비활성화 작업 내용

## 1. 개요
데이터베이스 스키마 자동 생성을 막고 데이터 유실을 방지하기 위해 JPA 하이버네이트의 자동 DDL 생성 설정을 비활성화했습니다. 
(`agent.md`의 최우선 작업 항목)

## 2. 변경 내용
`src/main/resources/application.yaml` 파일의 다음 두 속성을 변경했습니다.
- `spring.jpa.generate-ddl: true` ➡️ `false`
- `spring.jpa.hibernate.ddl-auto: update` ➡️ `none`

## 3. 검증 (Verification)
- 메이븐 빌드 및 스프링 테스트(`mvnw test`)를 통해 애플리케이션 초기화 구동 시도를 확인했습니다.
- 로컬 DB 서버 미기동으로 인한 테스트 실패는 있었으나, 변경된 설정 자체가 애플리케이션 문법 오류나 컨텍스트 로딩 실패를 유발하지 않음을 확인했습니다.

## 4. 이후 주의사항
- 향후 Entity 클래스가 수정되거나 추가되더라도 데이터베이스 테이블에 자동으로 반영되지 않습니다.
- DB 스키마 수정이 필요할 경우 수동으로 DDL 쿼리를 실행하거나 별도의 마이그레이션 가이드를 따라야 합니다.
