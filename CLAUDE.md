# KOSPI Alarm - Claude Code Project Guide

## Project Overview
Android application that monitors KOSPI index and sends notifications when specific percentage thresholds are reached.

### Key Features
- Real-time KOSPI index monitoring via Yahoo Finance API
- Percentage-based alerts: 5%, 10% (both rise and fall, max 2 alarms)
- Scheduled checks at :00, :15, :30, :45 every hour
- Daily notifications at 09:15 (market open) and 15:15 (market close)
- Market hours validation (Mon-Fri, 09:00-15:30)
- Investment strategy guide card
- Background monitoring with WorkManager
- Alarm auto-disable after trigger (prevents duplicates)
- Notification history (future feature)
- Multiple stock support (future feature)

## Technical Stack
- **Language**: Kotlin
- **Architecture**: MVVM + Clean Architecture (data/domain/presentation layers)
- **UI**: Jetpack Compose (Material Design 3)
- **DI**: Hilt
- **Database**: Room
- **Network**: Retrofit (Yahoo Finance API)
- **Background**: WorkManager (runs at :00, :15, :30, :45 every hour)
- **Logging**: Timber
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 34

## Architecture Guidelines

### Layer Structure
```
kim.yeonghoon.kospialarm/
├── data/           # Data sources, repositories implementation
│   ├── local/      # Room database, DAOs, entities
│   ├── remote/     # Retrofit API services, DTOs (Yahoo Finance)
│   └── repository/ # Repository implementations
├── domain/         # Business logic
│   ├── model/      # Domain models
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Use cases
├── presentation/   # UI layer
│   ├── ui/         # Compose screens
│   └── viewmodel/  # ViewModels
├── di/             # Hilt modules
├── worker/         # WorkManager workers (KospiCheckWorker, DailyKospiNotificationWorker)
└── util/           # Utilities, extensions (WorkManagerHelper, NotificationHelper)
```

### Naming Conventions
- **Entities**: `*Entity` (e.g., `AlarmEntity`)
- **DTOs**: `*Response`, `*Request` (e.g., `KospiResponse`)
- **Models**: Plain names (e.g., `Alarm`, `KospiData`)
- **UseCases**: Verb + Noun (e.g., `GetAlarmsUseCase`, `CreateAlarmUseCase`)
- **ViewModels**: `*ViewModel` (e.g., `MainViewModel`)
- **Screens**: `*Screen` (e.g., `AlarmListScreen`, `AlarmDetailScreen`)
- **Workers**: `*Worker` (e.g., `KospiCheckWorker`)

### Error Handling
Use sealed class for API responses:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### Retry Logic
- WorkManager checks KOSPI every 5 minutes
- Track consecutive failures (max 3 = 15 minutes)
- Send push notification after 3 consecutive failures
- Reset failure count to 0 on success

## Code Style

### General
- Follow [Google Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Maximum line length: 100 characters
- Use trailing commas in multi-line declarations

### Documentation
- **Required**: KDoc for all functions (public, private, internal)
- Format:
```kotlin
/**
 * Brief description of what this function does.
 *
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception occurs
 */
fun exampleFunction(paramName: String): Result<Data>
```

### Logging
- Use Timber for all logging
- Log levels:
  - `Timber.d()`: Debug info (development only)
  - `Timber.i()`: Important business logic flow
  - `Timber.w()`: Recoverable errors, retry attempts
  - `Timber.e()`: Critical errors, exceptions

## Testing Strategy

### Required Tests
1. **Unit Tests** (JUnit, MockK)
   - All UseCases
   - ViewModels
   - Repository implementations
   - Utility functions

2. **Integration Tests**
   - Room database operations
   - API services with mock server

3. **UI Tests** (Compose UI Test, Espresso)
   - Navigation flows
   - User interactions
   - Alert creation/deletion

### Test Guidelines
- Minimum coverage: 70%
- Test file naming: `*Test` (e.g., `GetAlarmsUseCaseTest`)
- Use `Given-When-Then` structure in test names:
```kotlin
@Test
fun `given valid alarm, when creating alarm, then should save to database`()
```

## Notification System

### Configuration
- **Channel**: "KOSPI Alerts"
- **Priority**: High (for heads-up notification)
- **Sound**: None
- **Vibration**: Pattern [0, 500, 200, 500] (vibrate-pause-vibrate)
- **LED**: None

### Notification Types
1. **Alert Triggered**: When threshold is reached
   - Tap action: Navigate to alert detail screen
2. **API Failure**: After 3 consecutive failures (15 minutes)
   - Tap action: Navigate to main screen

## Commit Message Format

Follow this convention (in Korean):
```
<type>(<scope>): <subject>

<body (optional)>
```

### Types
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `test`: 테스트 추가/수정
- `docs`: 문서 변경
- `style`: 코드 스타일 변경 (포맷팅)
- `chore`: 빌드, 의존성 등

### Examples
```
feat(alarm): 퍼센트 기반 알림 생성 추가

fix(worker): API 호출 성공 시 실패 카운트 리셋

test(repository): AlarmRepository 단위 테스트 추가
```

## Security Checklist

### Before Each Commit
- [ ] No hardcoded API keys or secrets
- [ ] No sensitive user data logged
- [ ] Input validation for user-entered data
- [ ] Proper error messages (no internal details exposed)
- [ ] Network requests use HTTPS only

### API Integration
- [ ] Validate API response structure
- [ ] Handle malformed JSON gracefully
- [ ] Set reasonable timeout values (15 seconds)
- [ ] Don't trust external data without validation

### Data Storage
- [ ] No sensitive data in SharedPreferences
- [ ] Room database uses appropriate data types
- [ ] No SQL injection vulnerabilities (use Room queries)

## WorkManager Configuration

### Constraints
```kotlin
Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED) // Need internet
    .build()
```

### Execution
- **Interval**: 15 minutes (PeriodicWorkRequest)
- **Schedule**: Runs at :00, :15, :30, :45 every hour (aligned to clock)
- **Initial Delay**: Calculated to next quarter hour
- **Flex Interval**: 5 minutes
- **Backoff Policy**: LINEAR (for retries if worker fails)
- **Policy**: REPLACE (applies new schedule on app restart)
- **Market Hours**: Only runs Mon-Fri 09:00-15:30
- **Daily Notifications**: 09:15 (market open), 15:15 (near market close)

## UI Guidelines

### Material Design 3
- Strictly follow Material Design 3 guidelines
- Use Material3 components only (no Material2)
- Support dynamic color scheme (Material You)
- Dark mode: Follow system setting

### Composable Naming
- Screens: `*Screen` (e.g., `AlarmListScreen`)
- Components: Descriptive names (e.g., `AlarmCard`, `PercentageSelector`)
- Preview functions: `*Preview` (e.g., `AlarmCardPreview`)

### State Management
- Use `remember` for simple UI state
- Use ViewModel for business logic state
- Hoist state when needed for reusability

## Dependencies Management

### Version Catalog (Future Enhancement)
Consider migrating to Gradle version catalogs for dependency management.

### Current Key Dependencies
- Compose BOM: 2023.10.01
- Room: 2.6.1
- Retrofit: 2.9.0
- Hilt: 2.48
- WorkManager: 2.9.0
- Timber: (to be added)

## Development Workflow

### Before Starting New Feature
1. Create feature branch from main
2. Update todo list if complex task
3. Write tests first (TDD preferred)
4. Implement feature
5. Run all tests - **must pass before commit**
6. Commit with proper message format

### Pull Request Checklist
- [ ] All tests passing
- [ ] New tests added for new features
- [ ] KDoc added for all new functions
- [ ] Security checklist reviewed
- [ ] No compiler warnings
- [ ] Material Design 3 compliance checked

## Known Limitations & Future Work

### Current Scope
- Single KOSPI index monitoring only
- No historical data visualization
- No alert editing (delete and recreate only)

### Future Features
- Multiple stock symbols support
- Alert history with charts
- Custom notification sounds
- Widget support
- Export alerts to CSV

## API Integration Notes

### KOSPI Data Source
- **API**: Yahoo Finance (`https://query1.finance.yahoo.com/v8/finance/chart/^KS11`)
- **Symbol**: ^KS11 (KOSPI Index)
- **Auth**: None required (free API)
- **Fields**: current index value, change, change percent, timestamp
- **Implementation**: `YahooFinanceKospiService`
- **Fallback**: Tracks consecutive failures, notifies after 3 failures (15 minutes)

### Response Handling
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
                val regularMarketPrice: Double,  // Current KOSPI value
                val previousClose: Double         // Previous close
            )
        }
    }
}
```

## Troubleshooting

### Common Issues
1. **WorkManager not running**: Check battery optimization settings
2. **Notifications not showing**: Verify notification permissions (Android 13+)
3. **API calls failing**: Check network connectivity and API endpoint status

## Contact & Support
- Project Owner: ioniere
- Issues: Track via GitHub issues (if applicable)
- Documentation: This file is the primary reference

---

**Last Updated**: 2025-10-29
**Version**: 1.0.0
**Repository**: https://github.com/kimyeonghoon/kospiAlram
