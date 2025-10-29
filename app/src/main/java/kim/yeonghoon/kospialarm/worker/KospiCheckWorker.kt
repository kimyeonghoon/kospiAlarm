package kim.yeonghoon.kospialarm.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kim.yeonghoon.kospialarm.data.local.dao.AlarmDao
import kim.yeonghoon.kospialarm.domain.model.AlarmHistory
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import kim.yeonghoon.kospialarm.util.NotificationHelper
import kim.yeonghoon.kospialarm.util.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Calendar

/**
 * 코스피 체크 WorkManager Worker.
 *
 * @property repository AlarmRepository
 * @property alarmDao AlarmDao
 */
@HiltWorker
class KospiCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: AlarmRepository,
    private val alarmDao: AlarmDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "kospi_check_work"
        private const val FAILURE_COUNT_KEY = "failure_count"
        private const val MAX_FAILURES = 3
    }

    /**
     * Worker 작업 실행.
     *
     * @return Result
     */
    override suspend fun doWork(): Result {
        Timber.d("KospiCheckWorker: 작업 시작")

        // 한국 주식 거래시간 체크 (평일 09:00~15:30)
        if (!isMarketOpen()) {
            Timber.i("KospiCheckWorker: 장 마감 시간 - 작업 스킵")
            return Result.success()
        }

        return try {
            val kospiResult = repository.getCurrentKospiData()

            when (kospiResult) {
                is kim.yeonghoon.kospialarm.util.Result.Success -> {
                    Timber.i("KospiCheckWorker: 코스피 데이터 조회 성공, index=${kospiResult.data.index}")

                    // 실패 카운트 리셋
                    resetFailureCount()

                    // 활성화된 알림 체크
                    checkAlarms(kospiResult.data.index)

                    Result.success()
                }

                is kim.yeonghoon.kospialarm.util.Result.Error -> {
                    Timber.w("KospiCheckWorker: 코스피 데이터 조회 실패 - ${kospiResult.exception.message}", kospiResult.exception)
                    handleFailure()
                    Result.retry()
                }

                is kim.yeonghoon.kospialarm.util.Result.Loading -> {
                    Timber.w("KospiCheckWorker: Loading 상태 - 재시도")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "KospiCheckWorker: 예외 발생")
            handleFailure()
            Result.retry()
        }
    }

    /**
     * 알림 조건 체크.
     *
     * @param currentIndex 현재 코스피 지수
     */
    private suspend fun checkAlarms(currentIndex: Double) {
        Timber.d("checkAlarms: 현재 지수=$currentIndex")

        val enabledAlarms = alarmDao.getEnabledAlarms()
        Timber.i("checkAlarms: ${enabledAlarms.size}개의 활성화된 알림 체크 시작")

        enabledAlarms.forEach { alarmEntity ->
            val alarm = alarmEntity.toDomain()
            val baseValue = alarm.baseValue
            val percentage = alarm.percentage
            val changePercent = ((currentIndex - baseValue) / baseValue) * 100

            Timber.d("checkAlarms: alarmId=${alarm.id}, baseValue=$baseValue, changePercent=$changePercent")

            val shouldTrigger = when (alarm.type) {
                AlarmType.RISE -> changePercent >= percentage
                AlarmType.FALL -> changePercent <= -percentage
            }

            if (shouldTrigger) {
                Timber.i("checkAlarms: 알림 트리거! alarmId=${alarm.id}")

                // 알림 표시
                NotificationHelper.showAlarmTriggeredNotification(
                    context = context,
                    alarmId = alarm.id,
                    baseValue = baseValue,
                    currentValue = currentIndex,
                    percentage = percentage,
                    type = alarm.type
                )

                // 히스토리 저장
                val history = AlarmHistory(
                    alarmId = alarm.id,
                    triggeredValue = currentIndex,
                    triggeredAt = System.currentTimeMillis(),
                    baseValue = baseValue,
                    percentage = percentage,
                    type = alarm.type
                )
                repository.saveAlarmHistory(history)

                // 알림 비활성화 (중복 발송 방지)
                alarmDao.updateAlarmEnabled(alarm.id, false)
                Timber.i("checkAlarms: 알림 비활성화됨, alarmId=${alarm.id}")

                Timber.i("checkAlarms: 알림 처리 완료, alarmId=${alarm.id}")
            }
        }
    }

    /**
     * 실패 처리.
     */
    private fun handleFailure() {
        val currentCount = inputData.getInt(FAILURE_COUNT_KEY, 0) + 1
        Timber.w("handleFailure: 실패 횟수=$currentCount")

        if (currentCount >= MAX_FAILURES) {
            Timber.e("handleFailure: 최대 실패 횟수 도달, 알림 표시")
            NotificationHelper.showApiFailureNotification(context)
        }
    }

    /**
     * 실패 카운트 리셋.
     */
    private fun resetFailureCount() {
        Timber.d("resetFailureCount: 실패 카운트 리셋")
        // 실패 카운트는 WorkManager의 inputData로 관리되므로
        // 성공 시 자동으로 리셋됨
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
