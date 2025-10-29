package kim.yeonghoon.kospialarm.util

import android.content.Context
import androidx.work.*
import kim.yeonghoon.kospialarm.worker.DailyKospiNotificationWorker
import kim.yeonghoon.kospialarm.worker.KospiCheckWorker
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * WorkManager 헬퍼 클래스.
 */
object WorkManagerHelper {

    private const val REPEAT_INTERVAL_MINUTES = 15L
    private const val FLEX_INTERVAL_MINUTES = 5L

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

    /**
     * 매일 정해진 시간에 KOSPI 알림 스케줄링.
     * 09:15 (장 시작 직후), 15:15 (장 마감 직전)
     *
     * @param context Context
     */
    fun scheduleDailyKospiNotifications(context: Context) {
        Timber.d("scheduleDailyKospiNotifications: 매일 알림 스케줄링 시작")

        // 09:15 알림
        scheduleDailyNotification(
            context,
            DailyKospiNotificationWorker.WORK_NAME_MORNING,
            DailyKospiNotificationWorker.TYPE_MORNING,
            9,
            15
        )

        // 15:15 알림
        scheduleDailyNotification(
            context,
            DailyKospiNotificationWorker.WORK_NAME_CLOSING,
            DailyKospiNotificationWorker.TYPE_CLOSING,
            15,
            15
        )

        Timber.i("scheduleDailyKospiNotifications: 매일 알림 스케줄링 완료")
    }

    /**
     * 특정 시간에 매일 알림 스케줄링.
     *
     * @param context Context
     * @param workName Work 이름
     * @param notificationType 알림 타입
     * @param hour 시간 (0-23)
     * @param minute 분 (0-59)
     */
    private fun scheduleDailyNotification(
        context: Context,
        workName: String,
        notificationType: String,
        hour: Int,
        minute: Int
    ) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 오늘의 목표 시간이 이미 지났다면 내일로 설정
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            DailyKospiNotificationWorker.KEY_NOTIFICATION_TYPE to notificationType
        )

        val workRequest = PeriodicWorkRequestBuilder<DailyKospiNotificationWorker>(
            1,
            TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Timber.d("scheduleDailyNotification: $workName 스케줄링 완료 (${hour}:${minute})")
    }

    /**
     * 매일 알림 중지.
     *
     * @param context Context
     */
    fun stopDailyKospiNotifications(context: Context) {
        Timber.d("stopDailyKospiNotifications: 매일 알림 중지")
        WorkManager.getInstance(context).cancelUniqueWork(DailyKospiNotificationWorker.WORK_NAME_MORNING)
        WorkManager.getInstance(context).cancelUniqueWork(DailyKospiNotificationWorker.WORK_NAME_CLOSING)
        Timber.i("stopDailyKospiNotifications: 매일 알림 중지 완료")
    }
}
