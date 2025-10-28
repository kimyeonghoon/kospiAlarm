# KOSPI Alarm

코스피 지수를 모니터링하고 설정한 퍼센트에 도달하면 알림을 보내주는 Android 앱입니다.

## 기능

- 📊 실시간 코스피 지수 모니터링 (5분마다)
- 🔔 퍼센트 기반 알림 (5%, 10%, 15%, 20% 상승/하락)
- 📱 여러 알림 조건 동시 설정
- 🔄 백그라운드 자동 체크 (WorkManager)
- 🎨 Material Design 3 UI
- 🌙 다크 모드 지원

## 기술 스택

- **언어**: Kotlin
- **아키텍처**: MVVM + Clean Architecture
- **UI**: Jetpack Compose
- **DI**: Hilt
- **데이터베이스**: Room
- **네트워크**: Retrofit + OkHttp
- **백그라운드**: WorkManager
- **로깅**: Timber

## 프로젝트 구조

```
com.ioniere.kospialarm/
├── data/           # 데이터 계층
│   ├── local/      # Room 데이터베이스
│   ├── remote/     # Retrofit API
│   └── repository/ # Repository 구현
├── domain/         # 비즈니스 로직
│   ├── model/      # 도메인 모델
│   ├── repository/ # Repository 인터페이스
│   └── usecase/    # Use Cases
├── presentation/   # UI 계층
│   ├── ui/         # Compose 화면
│   └── viewmodel/  # ViewModels
├── di/             # Hilt DI 모듈
├── worker/         # WorkManager Workers
└── util/           # 유틸리티
```

## 빌드 방법

### 1. 사전 요구사항

- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17
- Android SDK (API 28 이상)

### 2. 프로젝트 설정

1. 저장소 클론:
```bash
git clone <repository-url>
cd kospiAlarm
```

2. `local.properties` 파일 생성:
```bash
cp local.properties.template local.properties
```

3. `local.properties` 파일을 열어 Android SDK 경로 설정:
```properties
sdk.dir=/your/path/to/Android/Sdk
```

### 3. Android Studio에서 실행

1. Android Studio에서 프로젝트 열기
2. Gradle 동기화 대기
3. 에뮬레이터 또는 실제 기기 연결
4. Run 버튼 클릭

### 4. 명령줄에서 빌드

```bash
# Debug 빌드
./gradlew assembleDebug

# Release 빌드
./gradlew assembleRelease

# 테스트 실행
./gradlew test
```

## 현재 상태

### ✅ 구현 완료
- Domain 계층 (모델, Repository, UseCase)
- Data 계층 (Room, Retrofit, Repository 구현)
- DI 설정 (Hilt)
- WorkManager 백그라운드 체크
- 알림 시스템
- ViewModel 및 UI (Compose)
- Mock API (테스트용)

### 🚧 향후 작업
- [ ] 실제 코스피 API 연동
- [ ] 알림 히스토리 화면
- [ ] 여러 종목 지원
- [ ] 위젯 추가
- [ ] 단위 테스트 작성
- [ ] UI 테스트 작성

## Mock API 사용

현재 실제 API가 연동되지 않아 Mock API를 사용합니다.
`MockKospiApiService`는 랜덤한 코스피 데이터를 생성합니다.

실제 API로 전환하려면:
1. `NetworkModule.kt`에서 `USE_MOCK_API = false`로 변경
2. `BASE_URL`을 실제 API 주소로 변경
3. 필요시 `KospiApiService` 인터페이스 수정

## 권한

앱이 요청하는 권한:
- `INTERNET`: API 호출
- `POST_NOTIFICATIONS`: 알림 표시 (Android 13+)

## 개발 가이드

자세한 개발 가이드는 다음 문서를 참고하세요:
- [CLAUDE.md](CLAUDE.md) - 영문 가이드
- [CLAUDE.ko.md](CLAUDE.ko.md) - 한글 가이드

## 라이선스

(라이선스 정보를 추가하세요)

## 기여

(기여 가이드를 추가하세요)
