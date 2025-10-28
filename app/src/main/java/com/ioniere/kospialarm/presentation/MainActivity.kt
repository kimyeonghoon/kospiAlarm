package com.ioniere.kospialarm.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ioniere.kospialarm.presentation.ui.AlarmListScreen
import com.ioniere.kospialarm.presentation.ui.theme.KospiAlarmTheme
import com.ioniere.kospialarm.util.NotificationHelper
import com.ioniere.kospialarm.util.WorkManagerHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * 메인 액티비티.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

        // 알림 채널 생성
        NotificationHelper.createNotificationChannel(this)

        // 알림 권한 요청 (Android 13+)
        checkAndRequestNotificationPermission()

        // WorkManager 시작
        WorkManagerHelper.startPeriodicKospiCheck(this)
        Timber.i("MainActivity: WorkManager 시작됨")

        setContent {
            KospiAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmListScreen()
                }
            }
        }
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
}
