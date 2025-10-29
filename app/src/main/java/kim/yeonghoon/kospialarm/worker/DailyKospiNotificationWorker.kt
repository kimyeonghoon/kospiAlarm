package kim.yeonghoon.kospialarm.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import kim.yeonghoon.kospialarm.util.NotificationHelper
import kim.yeonghoon.kospialarm.util.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Calendar

/**
 * 매일 정해진 시간에 KOSPI 지수를 알려주는 Worker.
 * 09:15 (장 시작 직후), 15:15 (장 마감 직전)
 *
 * @property repository AlarmRepository
 */
@HiltWorker
class DailyKospiNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AlarmRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME_MORNING = "daily_kospi_morning"
        const val WORK_NAME_CLOSING = "daily_kospi_closing"
        const val KEY_NOTIFICATION_TYPE = "notification_type"
        const val TYPE_MORNING = "morning"
        const val TYPE_CLOSING = "closing"
    }

    /**
     * Worker 작업 실행.
     *
     * @return Result
     */
    override suspend fun doWork(): Result {
        val notificationType = inputData.getString(KEY_NOTIFICATION_TYPE) ?: TYPE_MORNING
        Timber.d("DailyKospiNotificationWorker: 작업 시작, type=$notificationType")

        // 한국 주식 거래시간 체크 (평일 09:00~15:30)
        if (!isMarketOpen()) {
            Timber.i("DailyKospiNotificationWorker: 장 마감 시간 - 작업 스킵")
            return Result.success()
        }

        return try {
            val kospiResult = repository.getCurrentKospiData()

            when (kospiResult) {
                is kim.yeonghoon.kospialarm.util.Result.Success -> {
                    val data = kospiResult.data
                    Timber.i("DailyKospiNotificationWorker: 코스피 데이터 조회 성공, index=${data.index}")

                    val title = when (notificationType) {
                        TYPE_MORNING -> "📈 장 시작 (09:15) - KOSPI"
                        TYPE_CLOSING -> "📉 장 마감 임박 (15:15) - KOSPI"
                        else -> "KOSPI 지수"
                    }

                    val changeSymbol = if (data.changePercent >= 0) "▲" else "▼"
                    val message = String.format(
                        "현재: %.2f %s %.2f (%.2f%%)",
                        data.index,
                        changeSymbol,
                        kotlin.math.abs(data.change),
                        kotlin.math.abs(data.changePercent)
                    )

                    NotificationHelper.showDailyKospiNotification(
                        context,
                        title,
                        message
                    )

                    Timber.i("DailyKospiNotificationWorker: 알림 전송 완료")
                    Result.success()
                }

                is kim.yeonghoon.kospialarm.util.Result.Error -> {
                    Timber.w("DailyKospiNotificationWorker: 코스피 데이터 조회 실패 - ${kospiResult.exception.message}", kospiResult.exception)
                    Result.retry()
                }

                is kim.yeonghoon.kospialarm.util.Result.Loading -> {
                    Timber.w("DailyKospiNotificationWorker: Loading 상태 - 재시도")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "DailyKospiNotificationWorker: 예외 발생")
            Result.retry()
        }
    }

    /**
     * 한국 주식 시장 개장 시간 체크.
     * 평일(월~금) 09:00~15:30
     *
     * @return 개장 시간이면 true, 아니면 false
     */
    private fun isMarketOpen(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // 주말 체크 (토요일=7, 일요일=1)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            Timber.d("isMarketOpen: 주말 - 장 마감")
            return false
        }

        // 시간 체크 (09:00~15:30)
        val currentTimeInMinutes = hourOfDay * 60 + minute
        val marketOpenTime = 9 * 60  // 09:00
        val marketCloseTime = 15 * 60 + 30  // 15:30

        val isOpen = currentTimeInMinutes in marketOpenTime..marketCloseTime
        Timber.d("isMarketOpen: 현재시간=${hourOfDay}:${minute}, 개장여부=$isOpen")
        return isOpen
    }
}
