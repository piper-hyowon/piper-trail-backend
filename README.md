# Piper Trail Blog

## 기술 스택
- **Backend**: Java 21, Spring Boot 3.4, MongoDB
- **Frontend**: React, TypeScript
- **Infrastructure**: Railway, Vercel, Cloudflare R2

## 아키텍처

### 명령/조회 책임 분리
- `/command` - 쓰기 작업 (생성, 수정, 삭제)
- `/query` - 읽기 작업 (조회, 검색)

### 이벤트 기반 캐시 무효화
- PostCreatedEvent, PostUpdatedEvent 발행 시 자동 캐시 정리
- Spring ApplicationEventPublisher + @TransactionalEventListener
- PostViewedEvent -> 조회수 증가, 방문자 추적
- CommentCreatedEvednt -> 스팸 검사
- AdminLoginAttemptEvent -> 로그인 시도 기록
- 모든 도메인 이벤트 MongoDB에 저장(향후 활용 대비)


### 3단계 캐싱
- React Query(브라우저 메모리): 동일 페이지 재방문 시 API 호출 0회
- HTTP 캐싱(Etag + Cache-Control): 데이터 변경 없을 시 304 Not Modified, 응답 본문 생략
- Caffeine Cache (서버 메모리). DB 조회 생략

## 주요 구현

### 보안
- **관리자**: JWT + TOTP 2단계 인증
- **방문자**: 로그인 없이 댓글 작성 (스팸 필터링 + Rate Limiting)
1. 댓글 스팸 방지

<img width="539" height="126" alt="image" src="https://github.com/user-attachments/assets/296dd0a7-1b33-42d1-8f3d-1b9519932131" />

**Rist Score 계산 (0 ~ 1)**
- 키워드 필터링: 설정 파일 스팸 키워드 감지시 +0.3
- IP별 시간당 댓글 수: 5개 초과시 +0.4
- 특수문자 비율: 전체 텍스트의 30% 초과 시 +0.2
- Score >= 0.7: 자동 숨김 처리
- Score >= 0.4: 관리자 검토 대기 상태

2. 방문자 스탬프(엽서) Rate Limiting으로 도배 방지
**IP 기반 제한**
- 분당 3개, 시간당 10개
- ConcurrentHashMap으로 요청 이력 관리
- 매일 자정 2시간 이상 기록 자동 정리

### 성능
- MongoDB 집계 파이프라인으로 쿼리 최적화
- 50,000개 데이터 기준 응답시간 400ms 이하
- 캐시 적용 시 95% 성능 개선 (214ms → 10ms)

### 모니터링
- `/monitoring` - 캐시 히트율/미스율 측정(Caffeine 내장 통계), 서버사이드 렌더링
<img width="500" alt="image" src="https://github.com/user-attachments/assets/27379f70-c3a3-4f0f-9fb6-332ccc20e1fc" />

### 다국어 지원(한국어/영어)
- URL 파라미터로 언어 선택(?lang=en)
- 파라미터 없는 경우 Accept-Language 파싱
- Spring HandlerMethodArgumentResolver, 커스텀 어노테이션(@Language) 활용
- 적용 범위: 블로그 포스트/시리즈 조회 및 검색


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


## REST 원칙 컨셉 UI
<img width="717" height="499" alt="image" src="https://github.com/user-attachments/assets/05e1cb53-3d61-4a97-a260-a6ad68856c80" />


