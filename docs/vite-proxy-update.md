# vite.config.js 프록시 하드코딩 제거 작업 내용

## 1. 개요
프론트엔드(`stockGame_react`) 프로젝트에서 로컬 환경의 백엔드 주소(`http://localhost:8882`)가 프록시 설정에 하드코딩되어 있던 문제를 개선했습니다. (`agent.md` 스프린트 우선순위 2번 항목)

## 2. 변경 내용
- **`stockGame_react/.env` 파일 생성**: `VITE_API_URL=http://localhost:8882` 속성을 추가했습니다.
- **`stockGame_react/vite.config.js` 수정**:
  - `loadEnv`를 도입하여 실행 모드(mode)에 따라 `.env` 파일의 변수를 로드하도록 변경했습니다.
  - `/api`, `/ws` 프록시 경로의 `target` 값을 `env.VITE_API_URL` 값으로 동적 할당되도록 수정했습니다.

## 3. 검증 (Verification)
- `npm run build` 스크립트를 실행하여 `vite.config.js`에 문법 오류가 없고 환경 변수가 정상적으로 바인딩되어 빌드가 성공하는 것을 확인했습니다.

## 4. 기대 효과
- 향후 배포 환경이나 다른 백엔드 포트를 사용할 때 코드를 수정할 필요 없이 `.env` 파일만 수정하여 대응할 수 있습니다.
