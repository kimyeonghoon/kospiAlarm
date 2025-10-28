package com.ioniere.kospialarm.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ioniere.kospialarm.data.local.dao.AlarmDao
import com.ioniere.kospialarm.domain.model.AlarmHistory
import com.ioniere.kospialarm.domain.model.AlarmType
import com.ioniere.kospialarm.domain.repository.AlarmRepository
import com.ioniere.kospialarm.util.NotificationHelper
import com.ioniere.kospialarm.util.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

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

        return try {
            val kospiResult = repository.getCurrentKospiData()

            when (kospiResult) {
                is com.ioniere.kospialarm.util.Result.Success -> {
                    Timber.i("KospiCheckWorker: 코스피 데이터 조회 성공, index=${kospiResult.data.index}")

                    // 실패 카운트 리셋
                    resetFailureCount()

                    // 활성화된 알림 체크
                    checkAlarms(kospiResult.data.index)

                    Result.success()
                }

                is com.ioniere.kospialarm.util.Result.Error -> {
                    Timber.w("KospiCheckWorker: 코스피 데이터 조회 실패", kospiResult.exception)
                    handleFailure()
                    Result.retry()
                }

                is com.ioniere.kospialarm.util.Result.Loading -> {
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
}
