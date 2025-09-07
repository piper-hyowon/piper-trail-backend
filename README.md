# Piper Trail Blog

## 기술 스택
- **Backend**: Java 21, Spring Boot 3.4, MongoDB
- **Frontend**: React, TypeScript
- **Infrastructure**: Railway, Vercel, Cloudflare R2

## 아키텍처

### 명령/조회 책임 분리
- `/command` - 쓰기 작업 (생성, 수정, 삭제)
- `/query` - 읽기 작업 (조회, 검색)
- 각 책임에 맞는 독립적인 Controller/Service 구성

### 이벤트 기반 캐시 무효화
- PostCreatedEvent, PostUpdatedEvent 발행 시 자동 캐시 정리
- Spring ApplicationEventPublisher + @TransactionalEventListener

### 3단계 캐싱
- React Query (브라우저 메모리)
- HTTP ETag (304 응답)
- Caffeine Cache (서버 메모리)

## 주요 구현

### 보안
- **관리자**: JWT + TOTP 2단계 인증
- **방문자**: 로그인 없이 댓글 작성 (스팸 필터링 + Rate Limiting)

### 성능
- MongoDB 집계 파이프라인으로 쿼리 최적화
- 50,000개 데이터 기준 응답시간 400ms 이하
- 캐시 적용 시 95% 성능 개선 (214ms → 10ms)

### 모니터링
- `/monitoring` - 캐시 히트율, 시스템 상태 대시보드 (서버사이드 렌더링)

## 프로젝트 구조
```
src/main/java/com/piper_trail/blog/
├── command/             # 쓰기 작업
│   ├── auth/           # 인증
│   ├── comment/        # 댓글 생성/삭제
│   ├── post/          # 포스트 CUD
│   └── postcard/      # 방명록
├── query/              # 읽기 작업
│   ├── comment/       # 댓글 조회
│   ├── post/          # 포스트 조회
│   ├── series/        # 시리즈 조회
│   ├── statistics/    # 통계
│   └── monitoring/    # 모니터링
├── shared/            # 공통
│   ├── domain/        # 엔티티, Repository
│   ├── event/         # 도메인 이벤트
│   └── exception/     # 예외 처리
└── infrastructure/    # 인프라
    ├── security/      # JWT, TOTP
    ├── cache/         # Caffeine 설정
    └── storage/       # R2 이미지 저장
```