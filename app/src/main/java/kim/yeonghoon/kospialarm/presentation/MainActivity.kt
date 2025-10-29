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
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var isAuthenticated by mutableStateOf(false)

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
     * @param savedInstanceState 저장된 상태
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("MainActivity: onCreate")

        // 보안 플래그 설정 (최근 앱 목록에서 내용 숨김)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        Timber.d("MainActivity: FLAG_SECURE 설정 완료")

        // 알림 채널 생성
        NotificationHelper.createNotificationChannel(this)

        // 알림 권한 요청 (Android 13+)
        checkAndRequestNotificationPermission()

        // WorkManager 시작
        WorkManagerHelper.startPeriodicKospiCheck(this)
        Timber.i("MainActivity: WorkManager 시작됨")

        // 매일 알림 스케줄링 (09:15, 15:15)
        WorkManagerHelper.scheduleDailyKospiNotifications(this)
        Timber.i("MainActivity: 매일 알림 스케줄링 완료")

        setContent {
            KospiAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        AlarmListScreen()
                    } else {
                        // 인증 전에는 검은 화면 표시 (보안)
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
     */
    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity: onResume")

        // 백그라운드에서 돌아올 때마다 인증 초기화 및 체크
        isAuthenticated = false
        checkBiometricAuthentication()
    }

    /**
     * 알림 권한 확인 및 요청.
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Timber.d("MainActivity: 알림 권한 이미 허용됨")
                }

                else -> {
                    Timber.d("MainActivity: 알림 권한 요청")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    /**
     * 생체인증 확인.
     */
    private fun checkBiometricAuthentication() {
        // 생체인증이 비활성화되어 있으면 바로 통과
        if (!PreferencesHelper.isBiometricEnabled(this)) {
            Timber.i("checkBiometricAuthentication: 생체인증 비활성화됨, 바로 통과")
            isAuthenticated = true
            return
        }

        // 생체인증 가능 여부 확인
        if (!BiometricHelper.canAuthenticate(this)) {
            Timber.w("checkBiometricAuthentication: 생체인증 불가능, 바로 통과")
            isAuthenticated = true
            return
        }

        // 생체인증 프롬프트 표시
        BiometricHelper.showBiometricPrompt(
            activity = this,
            onSuccess = {
                Timber.i("checkBiometricAuthentication: 인증 성공")
                isAuthenticated = true
            },
            onError = { errorCode, errorMessage ->
                Timber.e("checkBiometricAuthentication: 인증 실패 - $errorCode: $errorMessage")
                // 에러 발생 시 앱 종료
                finish()
            }
        )
    }
}
