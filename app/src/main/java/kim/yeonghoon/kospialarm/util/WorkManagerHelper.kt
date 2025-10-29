package kim.yeonghoon.kospialarm.util

import android.content.Context
import androidx.work.*
import kim.yeonghoon.kospialarm.worker.KospiCheckWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * WorkManager 헬퍼 클래스.
 */
object WorkManagerHelper {

    private const val REPEAT_INTERVAL_MINUTES = 5L
    private const val FLEX_INTERVAL_MINUTES = 1L

    /**
     * 주기적인 코스피 체크 작업 시작.
     *
     * @param context Context
     */
    fun startPeriodicKospiCheck(context: Context) {
        Timber.d("startPeriodicKospiCheck: 주기적 작업 시작")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<KospiCheckWorker>(
            REPEAT_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
            FLEX_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10000L,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            KospiCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Timber.i("startPeriodicKospiCheck: 주기적 작업 등록 완료 (${REPEAT_INTERVAL_MINUTES}분 간격)")
    }

    /**
     * 주기적인 코스피 체크 작업 중지.
     *
     * @param context Context
     */
    fun stopPeriodicKospiCheck(context: Context) {
        Timber.d("stopPeriodicKospiCheck: 주기적 작업 중지")
        WorkManager.getInstance(context).cancelUniqueWork(KospiCheckWorker.WORK_NAME)
        Timber.i("stopPeriodicKospiCheck: 주기적 작업 중지 완료")
    }
}
