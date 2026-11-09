# barofit-analytics

**barofit** 백엔드의 운동 기록/통계 서비스 레포입니다. 원래 `backend_baro` 모노레포의
`services/analytics` 모듈을 폴리레포 전환 과정에서 분리했습니다(커밋 이력 보존).

[barofit-workout](https://github.com/Barofit-team/barofit-workout)이 DB 아웃박스 패턴으로
비동기 전달하는 운동 완료 기록을 수신(`PUT /internal/workout-completions/{sessionId}`, 게이트웨이에
노출되지 않는 내부 API)해서 일별 통계·캘린더를 집계하고, 프론트에는 기록/통계/캘린더 조회 API를
제공합니다.

## 기술 스택

- Java 21 / Gradle (단일 모듈, 독립 레포)
- Spring Boot 4.1 (Web MVC, Data JPA, Validation)
- PostgreSQL `analytics` 스키마(`currentSchema=analytics`), Flyway

## 의존성

[barofit-common](https://github.com/Barofit-team/barofit-common)의 `barofit-runtime`을 참조합니다.
먼저 그 레포에서 `./gradlew publishToMavenLocal`을 실행해둬야 이 레포가 빌드됩니다.

`ANALYTICS_INTERNAL_TOKEN`은 workout 서비스가 보내는 내부 요청을 검증하는 공유 토큰입니다
(상수시간 비교, 미설정 시 수신 API는 503).

## 로컬 실행

```bash
cp .env.example .env   # 레포에 .env.example이 없다면 barofit-infra의 것을 참고
./gradlew bootRun       # 기본 포트 3007
```

## 테스트 / 빌드

```bash
./gradlew test    # 일부 통합 테스트는 로컬 5432에 실제 Postgres(barofit_analytics_test DB) 필요
./gradlew build   # build/libs/app.jar
```

## Docker 이미지

```bash
DOCKERHUB_USER=<hub-user> ./scripts/build-and-push.sh
```

## 배포

로컬 minikube 배포는 [barofit-infra](https://github.com/Barofit-team/barofit-infra)의
`infra/charts/analytics` Helm 차트로 합니다.

## API 요약

- `PUT /internal/workout-completions/{sessionId}` — workout 서비스 전용 내부 수신 API(게이트웨이 미노출)
- `GET /api/v1/workout-records/*`, `/api/v1/statistics/*`, `/api/v1/calendar/*` — 조회 API (JWT 필요)
- Swagger UI: `/swagger-ui.html`, OpenAPI: `/v3/api-docs`

## 관련 레포

- [barofit-common](https://github.com/Barofit-team/barofit-common) — 공유 라이브러리(이 레포가 의존)
- [barofit-identity](https://github.com/Barofit-team/barofit-identity) · [barofit-routine](https://github.com/Barofit-team/barofit-routine) · [barofit-workout](https://github.com/Barofit-team/barofit-workout)(아웃박스 전달 출처) · [barofit-report](https://github.com/Barofit-team/barofit-report)
- [barofit-infra](https://github.com/Barofit-team/barofit-infra) — Helm 차트, 로컬 검증 스크립트
- [backend_baro](https://github.com/Barofit-team/backend_baro) — 분리 이전 모노레포(참고용)
