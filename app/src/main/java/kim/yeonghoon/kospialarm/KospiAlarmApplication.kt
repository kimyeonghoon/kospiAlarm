package kim.yeonghoon.kospialarm

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * KOSPI Alarm Application 클래스.
 * Hilt 및 Timber 초기화.
 */
@HiltAndroidApp
class KospiAlarmApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Application 생성 시 초기화.
     */
    override fun onCreate() {
        super.onCreate()

        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Application created - Debug mode")
        }

        Timber.i("KospiAlarmApplication initialized")
    }

    /**
     * WorkManager Configuration 제공.
     * HiltWorkerFactory를 사용하여 Worker에 의존성 주입.
     *
     * @return WorkManager Configuration
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
