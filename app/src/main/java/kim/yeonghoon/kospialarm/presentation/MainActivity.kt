package kim.yeonghoon.kospialarm.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import kim.yeonghoon.kospialarm.presentation.ui.AlarmListScreen
import kim.yeonghoon.kospialarm.presentation.ui.theme.KospiAlarmTheme
import kim.yeonghoon.kospialarm.util.BiometricHelper
import kim.yeonghoon.kospialarm.util.NotificationHelper
import kim.yeonghoon.kospialarm.util.PreferencesHelper
import kim.yeonghoon.kospialarm.util.WorkManagerHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 메인 액티비티.
 *
 * KOSPI Alarm 앱의 진입점이자 유일한 Activity입니다.
 * 이 Activity는 다음 기능들을 담당합니다:
 *
 * 1. 생체인증 (Biometric Authentication)
 *    - 앱 실행 시 지문인증 요구
 *    - 백그라운드에서 복귀 시 재인증 요구
 *    - FLAG_SECURE로 최근 앱 목록 보안
 *
 * 2. 알림 권한 관리
 *    - Android 13+ 런타임 권한 요청
 *    - 알림 채널 생성
 *
 * 3. 백그라운드 작업 스케줄링
 *    - WorkManager로 주기적 코스피 체크 (매시 0, 15, 30, 45분)
 *    - 일일 알림 스케줄링 (09:15, 15:15)
 *
 * 4. UI 렌더링
 *    - Jetpack Compose로 알림 목록 화면 표시
 *    - Material Design 3 테마 적용
 *
 * 상속: FragmentActivity
 * - ComponentActivity 대신 FragmentActivity를 사용하는 이유:
 *   BiometricPrompt API가 FragmentActivity를 요구하기 때문
 *
 * Hilt 의존성 주입:
 * - @AndroidEntryPoint 애노테이션으로 Hilt 통합
 * - ViewModel, Repository 등의 자동 주입
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    /**
     * 생체인증 성공 여부를 나타내는 상태 변수.
     *
     * Compose의 mutableStateOf를 사용하여 상태 변경 시 자동으로 UI를 재구성합니다.
     * - false: 미인증 상태, 검은 화면 표시
     * - true: 인증 성공, AlarmListScreen 표시
     *
     * onResume()에서 매번 false로 초기화되어 재인증을 요구합니다.
     */
    private var isAuthenticated by mutableStateOf(false)

    /**
     * 알림 권한 요청 결과를 처리하는 ActivityResultLauncher.
     *
     * Android 13 (API 33) 이상에서는 POST_NOTIFICATIONS 권한이 필요합니다.
     * 사용자의 허용/거부 결과를 콜백으로 받아 로그를 남깁니다.
     *
     * 참고:
     * - 권한이 거부되어도 앱 실행은 계속됩니다.
     * - 알림을 보낼 수 없지만 앱의 핵심 기능(알림 목록 보기)은 사용 가능합니다.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.i("MainActivity: 알림 권한 허용됨")
        } else {
            Timber.w("MainActivity: 알림 권한 거부됨")
        }
    }

    /**
     * Activity 생성.
     *
     * Activity가 최초 생성될 때 한 번만 호출됩니다.
     * 앱의 초기 설정과 백그라운드 작업 스케줄링을 수행합니다.
     *
     * 실행 순서:
     * 1. 보안 플래그 설정 (FLAG_SECURE)
     * 2. 알림 채널 생성
     * 3. 알림 권한 요청 (Android 13+)
     * 4. WorkManager 시작 (코스피 주기 체크)
     * 5. 일일 알림 스케줄링
     * 6. Compose UI 설정
     *
     * 참고:
     * - 생체인증은 onResume()에서 수행됩니다.
     * - onCreate()는 Activity 생성 시 한 번만 호출되지만,
     *   onResume()은 백그라운드에서 돌아올 때마다 호출됩니다.
     *
     * @param savedInstanceState 이전 상태 복원을 위한 Bundle (현재 미사용)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("MainActivity: onCreate")

        // 1. 보안 플래그 설정 (최근 앱 목록에서 내용 숨김)
        // - 최근 앱 목록에서 검은 화면으로 표시
        // - 스크린샷 차단 (민감 정보 보호)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        Timber.d("MainActivity: FLAG_SECURE 설정 완료")

        // 2. 알림 채널 생성
        // - Android 8.0+ 필수
        // - 앱 설치 후 최초 한 번만 생성되면 되지만, 매번 호출해도 무방
        NotificationHelper.createNotificationChannel(this)

        // 3. 알림 권한 요청 (Android 13+)
        // - Android 13부터 POST_NOTIFICATIONS 런타임 권한 필요
        // - 이전 버전에서는 자동으로 허용됨
        checkAndRequestNotificationPermission()

        // 4. WorkManager 시작 (코스피 주기 체크)
        // - 매시 0, 15, 30, 45분에 실행되도록 스케줄링
        // - 시계 정렬 방식으로 정확한 시간에 실행
        WorkManagerHelper.startPeriodicKospiCheck(this)
        Timber.i("MainActivity: WorkManager 시작됨")

        // 5. 매일 알림 스케줄링 (09:15, 15:15)
        // - 장 시작(09:15), 장 마감 임박(15:15)에 푸시 알림
        WorkManagerHelper.scheduleDailyKospiNotifications(this)
        Timber.i("MainActivity: 매일 알림 스케줄링 완료")

        // 6. Compose UI 설정
        setContent {
            KospiAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 생체인증 상태에 따라 화면 전환
                    if (isAuthenticated) {
                        // 인증 성공: 알림 목록 화면 표시
                        AlarmListScreen()
                    } else {
                        // 미인증: 검은 화면 표시 (보안)
                        // - 생체인증 프롬프트가 표시될 때까지 내용 숨김
                        // - onResume()에서 checkBiometricAuthentication() 호출 시 프롬프트 표시
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        )
                    }
                }
            }
        }
    }

    /**
     * Activity가 포그라운드로 올 때.
     *
     * 다음 상황에서 호출됩니다:
     * - 앱 최초 실행 (onCreate 직후)
     * - 백그라운드에서 복귀
     * - 다른 Activity에서 돌아옴
     *
     * 생체인증 전략:
     * - 매번 인증 상태를 초기화(false)하여 재인증을 요구합니다.
     * - 이로 인해 백그라운드에서 돌아올 때마다 지문인증이 필요합니다.
     * - 보안을 강화하기 위한 의도적인 설계입니다.
     */
    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity: onResume")

        // 백그라운드에서 돌아올 때마다 인증 초기화 및 체크
        // 1. isAuthenticated를 false로 설정 → UI가 검은 화면으로 전환
        // 2. checkBiometricAuthentication() 호출 → 생체인증 프롬프트 표시
        isAuthenticated = false
        checkBiometricAuthentication()
    }

    /**
     * 알림 권한 확인 및 요청.
     *
     * Android 13 (API 33) 이상에서만 실행됩니다.
     * POST_NOTIFICATIONS 런타임 권한이 필요하며, 이전 버전에서는 자동 허용됩니다.
     *
     * 권한 확인 흐름:
     * 1. Android 버전 체크 (13 이상)
     * 2. 권한 이미 허용됨 → 로그만 남기고 종료
     * 3. 권한 미허용 → requestPermissionLauncher로 권한 요청 다이얼로그 표시
     *
     * 참고:
     * - 사용자가 권한을 거부해도 앱은 정상 동작합니다.
     * - 다만 알림을 보낼 수 없어 주요 기능이 제한됩니다.
     */
    private fun checkAndRequestNotificationPermission() {
        // Android 13 (Tiramisu, API 33) 이상에서만 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // 이미 권한이 허용된 경우
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Timber.d("MainActivity: 알림 권한 이미 허용됨")
                }

                // 권한이 없는 경우 → 요청 다이얼로그 표시
                else -> {
                    Timber.d("MainActivity: 알림 권한 요청")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    /**
     * 생체인증 확인.
     *
     * 생체인증이 필요한지 판단하고, 필요하다면 프롬프트를 표시합니다.
     *
     * 인증 스킵 조건:
     * 1. 사용자가 설정에서 생체인증을 비활성화한 경우
     * 2. 디바이스가 생체인증을 지원하지 않는 경우
     * 3. 사용자가 생체정보를 등록하지 않은 경우
     *
     * 인증 성공 시:
     * - isAuthenticated = true → AlarmListScreen 표시
     *
     * 인증 에러 시 (타임아웃, 하드웨어 오류 등):
     * - finish() 호출하여 앱 종료
     * - 사용자 취소(NEGATIVE_BUTTON, USER_CANCELED)는 에러로 처리하지 않음
     */
    private fun checkBiometricAuthentication() {
        // 1. 생체인증이 설정에서 비활성화되어 있으면 바로 통과
        if (!PreferencesHelper.isBiometricEnabled(this)) {
            Timber.i("checkBiometricAuthentication: 생체인증 비활성화됨, 바로 통과")
            isAuthenticated = true
            return
        }

        // 2. 생체인증 하드웨어가 없거나 등록된 생체정보가 없으면 바로 통과
        //    (보안을 강제할 수 없는 상황)
        if (!BiometricHelper.canAuthenticate(this)) {
            Timber.w("checkBiometricAuthentication: 생체인증 불가능, 바로 통과")
            isAuthenticated = true
            return
        }

        // 3. 생체인증 프롬프트 표시
        BiometricHelper.showBiometricPrompt(
            activity = this,
            onSuccess = {
                // 인증 성공: UI를 AlarmListScreen으로 전환
                Timber.i("checkBiometricAuthentication: 인증 성공")
                isAuthenticated = true
            },
            onError = { errorCode, errorMessage ->
                // 심각한 에러 (타임아웃, 하드웨어 오류 등): 앱 종료
                // 사용자 취소(NEGATIVE_BUTTON, USER_CANCELED)는 여기로 오지 않음
                Timber.e("checkBiometricAuthentication: 인증 실패 - $errorCode: $errorMessage")
                finish()
            }
        )
    }
}
