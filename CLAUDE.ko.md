# KOSPI Alarm - Claude Code 프로젝트 가이드

## 프로젝트 개요
코스피 지수를 모니터링하고 특정 퍼센트 임계값에 도달하면 알림을 보내는 Android 애플리케이션입니다.

### 주요 기능
- Yahoo Finance API를 통한 실시간 코스피 지수 모니터링
- 퍼센트 기반 알림: 5%, 10% (상승 및 하락, 최대 2개)
- 매시 정시 체크: 0분, 15분, 30분, 45분
- 일일 알림: 09:15 (장 시작), 15:15 (장 마감 임박)
- 장 시간 검증 (월~금, 09:00-15:30)
- 투자 전략 가이드 카드
- WorkManager를 통한 백그라운드 모니터링
- 알림 자동 비활성화 (중복 방지)
- 알림 히스토리 (향후 기능)
- 여러 종목 지원 (향후 기능)

## 기술 스택
- **언어**: Kotlin
- **아키텍처**: MVVM + Clean Architecture (data/domain/presentation 계층)
- **UI**: Jetpack Compose (Material Design 3)
- **DI**: Hilt
- **데이터베이스**: Room
- **네트워크**: Retrofit (Yahoo Finance API)
- **백그라운드**: WorkManager (매시 :00, :15, :30, :45 실행)
- **로깅**: Timber
- **최소 SDK**: 28 (Android 9.0)
- **타겟 SDK**: 34

## 아키텍처 가이드라인

### 계층 구조
```
kim.yeonghoon.kospialarm/
├── data/           # 데이터 소스, 레포지토리 구현
│   ├── local/      # Room 데이터베이스, DAO, 엔티티
│   ├── remote/     # Retrofit API 서비스, DTO (Yahoo Finance)
│   └── repository/ # 레포지토리 구현체
├── domain/         # 비즈니스 로직
│   ├── model/      # 도메인 모델
│   ├── repository/ # 레포지토리 인터페이스
│   └── usecase/    # 유스케이스
├── presentation/   # UI 계층
│   ├── ui/         # Compose 스크린
│   └── viewmodel/  # 뷰모델
├── di/             # Hilt 모듈
├── worker/         # WorkManager 워커 (KospiCheckWorker, DailyKospiNotificationWorker)
└── util/           # 유틸리티, 확장 함수 (WorkManagerHelper, NotificationHelper)
```

### 네이밍 컨벤션
- **엔티티**: `*Entity` (예: `AlarmEntity`)
- **DTO**: `*Response`, `*Request` (예: `KospiResponse`)
- **모델**: 일반 이름 (예: `Alarm`, `KospiData`)
- **유스케이스**: 동사 + 명사 (예: `GetAlarmsUseCase`, `CreateAlarmUseCase`)
- **뷰모델**: `*ViewModel` (예: `MainViewModel`)
- **스크린**: `*Screen` (예: `AlarmListScreen`, `AlarmDetailScreen`)
- **워커**: `*Worker` (예: `KospiCheckWorker`)

### 에러 처리
API 응답을 위한 sealed class 사용:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 재시도 로직
- WorkManager가 5분마다 코스피 체크
- 연속 실패 횟수 추적 (최대 3회 = 15분)
- 3회 연속 실패 후 푸시 알림 발송
- 성공 시 실패 카운트를 0으로 리셋

## 코드 스타일

### 일반 사항
- [Google Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) 준수
- 최대 줄 길이: 100자
- 여러 줄 선언 시 후행 쉼표 사용

### 문서화
- **필수**: 모든 함수에 KDoc 작성 (public, private, internal)
- 형식:
```kotlin
/**
 * 이 함수가 무엇을 하는지에 대한 간단한 설명.
 *
 * @param paramName 파라미터 설명
 * @return 반환값 설명
 * @throws ExceptionType 이 예외가 발생하는 경우
 */
fun exampleFunction(paramName: String): Result<Data>
```

### 로깅
- 모든 로깅에 Timber 사용
- 로그 레벨:
  - `Timber.d()`: 디버그 정보 (개발 중에만)
  - `Timber.i()`: 중요한 비즈니스 로직 흐름
  - `Timber.w()`: 복구 가능한 에러, 재시도 시도
  - `Timber.e()`: 치명적 에러, 예외

## 테스트 전략

### 필수 테스트
1. **단위 테스트** (JUnit, MockK)
   - 모든 유스케이스
   - 뷰모델
   - 레포지토리 구현체
   - 유틸리티 함수

2. **통합 테스트**
   - Room 데이터베이스 작업
   - Mock 서버를 사용한 API 서비스

3. **UI 테스트** (Compose UI Test, Espresso)
   - 네비게이션 흐름
   - 사용자 인터랙션
   - 알림 생성/삭제

### 테스트 가이드라인
- 최소 커버리지: 70%
- 테스트 파일 네이밍: `*Test` (예: `GetAlarmsUseCaseTest`)
- 테스트 이름에 `Given-When-Then` 구조 사용:
```kotlin
@Test
fun `given valid alarm, when creating alarm, then should save to database`()
```

## 알림 시스템

### 설정
- **채널**: "KOSPI Alerts"
- **우선순위**: High (헤드업 알림용)
- **소리**: 없음
- **진동**: 패턴 [0, 500, 200, 500] (진동-멈춤-진동)
- **LED**: 없음

### 알림 유형
1. **알림 트리거**: 임계값 도달 시
   - 탭 동작: 알림 상세 화면으로 이동
2. **API 실패**: 3회 연속 실패 후 (15분)
   - 탭 동작: 메인 화면으로 이동

## 커밋 메시지 형식

다음 컨벤션을 따릅니다:
```
<type>(<scope>): <subject>

<body (선택사항)>
```

### 타입
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 변경
- `style`: 코드 스타일 변경 (포맷팅)
- `chore`: 빌드, 의존성 등

### 예시
```
feat(alarm): add percentage-based alert creation

fix(worker): reset failure count on successful API call

test(repository): add unit tests for AlarmRepository
```

## 보안 체크리스트

### 각 커밋 전
- [ ] 하드코딩된 API 키 또는 비밀 정보 없음
- [ ] 민감한 사용자 데이터 로그 없음
- [ ] 사용자 입력 데이터 유효성 검사
- [ ] 적절한 에러 메시지 (내부 세부사항 노출 안 됨)
- [ ] 네트워크 요청은 HTTPS만 사용

### API 통합
- [ ] API 응답 구조 검증
- [ ] 잘못된 JSON을 우아하게 처리
- [ ] 합리적인 타임아웃 값 설정 (15초)
- [ ] 검증 없이 외부 데이터 신뢰하지 않음

### 데이터 저장
- [ ] SharedPreferences에 민감한 데이터 없음
- [ ] Room 데이터베이스가 적절한 데이터 타입 사용
- [ ] SQL 인젝션 취약점 없음 (Room 쿼리 사용)

## WorkManager 설정

### 제약 조건
```kotlin
Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED) // 인터넷 필요
    .build()
```

### 실행
- **간격**: 15분 (PeriodicWorkRequest)
- **스케줄**: 매시 :00, :15, :30, :45 실행 (시계에 정렬)
- **초기 지연**: 다음 15분 단위까지 계산
- **Flex 간격**: 5분
- **백오프 정책**: LINEAR (워커 실패 시 재시도용)
- **정책**: REPLACE (앱 재시작 시 새 스케줄 적용)
- **장 시간**: 월~금 09:00-15:30만 실행
- **일일 알림**: 09:15 (장 시작), 15:15 (장 마감 임박)

## UI 가이드라인

### Material Design 3
- Material Design 3 가이드라인을 엄격히 준수
- Material3 컴포넌트만 사용 (Material2 사용 금지)
- 동적 색상 스킴 지원 (Material You)
- 다크 모드: 시스템 설정 따름

### Composable 네이밍
- 스크린: `*Screen` (예: `AlarmListScreen`)
- 컴포넌트: 설명적 이름 (예: `AlarmCard`, `PercentageSelector`)
- 프리뷰 함수: `*Preview` (예: `AlarmCardPreview`)

### 상태 관리
- 간단한 UI 상태는 `remember` 사용
- 비즈니스 로직 상태는 ViewModel 사용
- 재사용성을 위해 필요시 상태 호이스팅

## 의존성 관리

### Version Catalog (향후 개선)
의존성 관리를 위해 Gradle version catalog로 마이그레이션 고려.

### 현재 주요 의존성
- Compose BOM: 2023.10.01
- Room: 2.6.1
- Retrofit: 2.9.0
- Hilt: 2.48
- WorkManager: 2.9.0
- Timber: (추가 예정)

## 개발 워크플로우

### 새 기능 시작 전
1. main에서 feature 브랜치 생성
2. 복잡한 작업인 경우 todo 리스트 업데이트
3. 테스트를 먼저 작성 (TDD 권장)
4. 기능 구현
5. 모든 테스트 실행 - **커밋 전 반드시 통과해야 함**
6. 적절한 메시지 형식으로 커밋

### Pull Request 체크리스트
- [ ] 모든 테스트 통과
- [ ] 새 기능에 대한 새 테스트 추가됨
- [ ] 모든 새 함수에 KDoc 추가됨
- [ ] 보안 체크리스트 검토됨
- [ ] 컴파일러 경고 없음
- [ ] Material Design 3 준수 확인됨

## 알려진 제한사항 및 향후 작업

### 현재 범위
- 단일 코스피 지수 모니터링만
- 과거 데이터 시각화 없음
- 알림 수정 불가 (삭제 후 재생성만 가능)

### 향후 기능
- 여러 종목 심볼 지원
- 차트가 포함된 알림 히스토리
- 커스텀 알림 소리
- 위젯 지원
- 알림을 CSV로 내보내기

## API 통합 참고사항

### 코스피 데이터 소스
- **API**: Yahoo Finance (`https://query1.finance.yahoo.com/v8/finance/chart/^KS11`)
- **심볼**: ^KS11 (KOSPI 지수)
- **인증**: 불필요 (무료 API)
- **필드**: 현재 지수 값, 변화량, 변화율, 타임스탬프
- **구현**: `YahooFinanceKospiService`
- **폴백**: 연속 실패 추적, 3회 실패 후 알림 (15분)

### 응답 처리
```kotlin
data class YahooFinanceResponse(
    val chart: Chart
) {
    data class Chart(
        val result: List<Result>
    ) {
        data class Result(
            val meta: Meta,
            val indicators: Indicators
        ) {
            data class Meta(
                val regularMarketPrice: Double,  // 현재 코스피 값
                val previousClose: Double         // 전일 종가
            )
        }
    }
}
```

## 문제 해결

### 일반적인 문제
1. **WorkManager가 실행되지 않음**: 배터리 최적화 설정 확인
2. **알림이 표시되지 않음**: 알림 권한 확인 (Android 13+)
3. **API 호출 실패**: 네트워크 연결 및 API 엔드포인트 상태 확인

## 연락처 및 지원
- 프로젝트 소유자: ioniere
- 이슈: GitHub 이슈를 통해 추적 (해당하는 경우)
- 문서: 이 파일이 주요 참조 문서입니다

---

**최종 업데이트**: 2025-10-29
**버전**: 1.0.0
**저장소**: https://github.com/kimyeonghoon/kospiAlarm
