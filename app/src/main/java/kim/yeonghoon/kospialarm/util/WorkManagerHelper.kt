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
 *
 * Android WorkManager API를 사용하여 백그라운드 작업을 스케줄링합니다.
 * 이 클래스는 다음 두 가지 유형의 백그라운드 작업을 관리합니다:
 *
 * 1. 주기적 코스피 체크 (KospiCheckWorker)
 *    - 매시 0, 15, 30, 45분에 실행 (시계 정렬)
 *    - 15분 주기, 5분 flex interval
 *    - 네트워크 연결 필수
 *
 * 2. 일일 코스피 알림 (DailyKospiNotificationWorker)
 *    - 09:15 (장 시작 직후)
 *    - 15:15 (장 마감 임박)
 *    - 매일 반복
 *
 * WorkManager 특징:
 * - 앱이 종료되어도 작업이 유지됨 (시스템 레벨 스케줄링)
 * - 배터리 최적화 및 Doze 모드 고려
 * - 네트워크, 배터리 등의 제약 조건 설정 가능
 * - 작업 실패 시 자동 재시도 (Backoff Policy)
 */
object WorkManagerHelper {

    /** 주기적 작업 반복 간격 (15분) */
    private const val REPEAT_INTERVAL_MINUTES = 15L

    /** 유연한 실행 시간 범위 (5분) - PeriodicWorkRequest에서 정확한 시간 제어 */
    private const val FLEX_INTERVAL_MINUTES = 5L

    /**
     * 주기적인 코스피 체크 작업 시작.
     *
     * 매시 정시(0, 15, 30, 45분)에 코스피 지수를 확인하는 작업을 스케줄링합니다.
     *
     * 시계 정렬 알고리즘:
     * 1. 현재 시각 확인
     * 2. 다음 15분 단위 시각 계산 (예: 12:07 → 12:15)
     * 3. 대기 시간 계산 (초 단위)
     * 4. initialDelay로 설정하여 정확한 시각에 시작
     *
     * WorkManager 설정:
     * - 반복 간격: 15분
     * - Flex 간격: 5분 (실제 실행 시각 ±5분 허용)
     * - 제약 조건: 네트워크 연결 필수
     * - Backoff Policy: LINEAR, 10초 간격으로 재시도
     * - ExistingWorkPolicy: REPLACE (새 스케줄로 교체)
     *
     * 참고:
     * - 앱 재시작 시 기존 작업을 취소하고 새로 등록합니다.
     * - 이를 통해 항상 정확한 시각에 실행되도록 보장합니다.
     *
     * @param context Context 인스턴스
     */
    fun startPeriodicKospiCheck(context: Context) {
        Timber.d("startPeriodicKospiCheck: 주기적 작업 시작")

        // 1. 다음 정시(0, 15, 30, 45분)까지의 시간 계산
        val currentTime = Calendar.getInstance()
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val currentSecond = currentTime.get(Calendar.SECOND)

        // 2. 다음 정시 분 계산 (0, 15, 30, 45)
        // 예: 12:07 → 15분, 12:23 → 30분, 12:47 → 0분(다음 시간)
        val nextQuarterMinute = when {
            currentMinute < 15 -> 15
            currentMinute < 30 -> 30
            currentMinute < 45 -> 45
            else -> 0  // 다음 시간의 0분
        }

        // 3. 다음 정시까지의 시간 계산 (초 단위)
        val minutesUntilNext = if (nextQuarterMinute == 0) {
            // 다음 시간 0분까지 (예: 12:47 → 13:00, 13분 대기)
            60 - currentMinute
        } else {
            // 현재 시간대 내 다음 정시까지 (예: 12:07 → 12:15, 8분 대기)
            nextQuarterMinute - currentMinute
        }
        // 초 단위로 변환하고, 현재 초를 빼서 정확한 대기 시간 계산
        val secondsUntilNext = (minutesUntilNext * 60) - currentSecond

        Timber.d("startPeriodicKospiCheck: 현재 시각=${currentMinute}:${currentSecond}, 다음 실행=${nextQuarterMinute}분, 대기시간=${secondsUntilNext}초")

        // 4. 네트워크 제약 조건 설정
        // CONNECTED: WiFi 또는 모바일 데이터 연결 필요
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 5. PeriodicWorkRequest 생성
        val workRequest = PeriodicWorkRequestBuilder<KospiCheckWorker>(
            REPEAT_INTERVAL_MINUTES,  // 15분 주기
            TimeUnit.MINUTES,
            FLEX_INTERVAL_MINUTES,    // ±5분 유연성
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(secondsUntilNext.toLong(), TimeUnit.SECONDS)  // 첫 실행까지 대기 시간
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,  // 선형 백오프 (실패 시 10초씩 증가)
                10000L,                // 10초 기본 대기
                TimeUnit.MILLISECONDS
            )
            .build()

        // 6. WorkManager에 작업 등록
        // REPLACE: 기존 작업이 있으면 취소하고 새로 등록 (앱 재시작 시 스케줄 재조정)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            KospiCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        Timber.i("startPeriodicKospiCheck: 주기적 작업 등록 완료 (매시 0, 15, 30, 45분 실행)")
    }

    /**
     * 주기적인 코스피 체크 작업 중지.
     *
     * 사용자가 설정에서 알림을 비활성화하거나,
     * 앱을 제거할 때 호출됩니다.
     *
     * 참고:
     * - cancelUniqueWork()는 진행 중인 작업도 즉시 취소합니다.
     * - 작업이 없어도 에러가 발생하지 않습니다.
     *
     * @param context Context 인스턴스
     */
    fun stopPeriodicKospiCheck(context: Context) {
        Timber.d("stopPeriodicKospiCheck: 주기적 작업 중지")
        WorkManager.getInstance(context).cancelUniqueWork(KospiCheckWorker.WORK_NAME)
        Timber.i("stopPeriodicKospiCheck: 주기적 작업 중지 완료")
    }

    /**
     * 매일 정해진 시간에 KOSPI 알림 스케줄링.
     *
     * 주식 거래 시간에 맞춰 다음 두 시각에 푸시 알림을 보냅니다:
     * - 09:15: 장 시작 직후 (코스피 개장 09:00 + 15분)
     * - 15:15: 장 마감 임박 (코스피 폐장 15:30 - 15분)
     *
     * 각 시각마다 별도의 Worker로 등록되며, 24시간 주기로 반복됩니다.
     *
     * @param context Context 인스턴스
     */
    fun scheduleDailyKospiNotifications(context: Context) {
        Timber.d("scheduleDailyKospiNotifications: 매일 알림 스케줄링 시작")

        // 09:15 알림 (장 시작 알림)
        scheduleDailyNotification(
            context,
            DailyKospiNotificationWorker.WORK_NAME_MORNING,
            DailyKospiNotificationWorker.TYPE_MORNING,
            9,
            15
        )

        // 15:15 알림 (장 마감 알림)
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
     * 지정된 시각에 매일 반복되는 알림을 등록합니다.
     *
     * 스케줄링 로직:
     * 1. 오늘 목표 시각 계산 (예: 오늘 09:15)
     * 2. 목표 시각이 이미 지났으면 내일로 설정
     * 3. 현재 시각부터 목표 시각까지의 밀리초 계산
     * 4. initialDelay로 설정하여 첫 실행 예약
     * 5. 이후 24시간마다 자동 반복
     *
     * ExistingWorkPolicy:
     * - KEEP: 기존 작업이 있으면 유지 (중복 등록 방지)
     * - 앱 재시작 시에도 기존 스케줄 유지
     *
     * @param context Context 인스턴스
     * @param workName 작업 고유 이름 (중복 방지용)
     * @param notificationType 알림 타입 (Worker에 전달되는 파라미터)
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
        // 1. 현재 시각 가져오기
        val currentTime = Calendar.getInstance()

        // 2. 오늘의 목표 시각 설정 (예: 오늘 09:15:00.000)
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 3. 오늘의 목표 시간이 이미 지났다면 내일로 설정
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 4. 첫 실행까지의 대기 시간 계산 (밀리초)
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        // 5. 네트워크 제약 조건 설정
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 6. Worker에 전달할 데이터 설정 (알림 타입)
        val inputData = workDataOf(
            DailyKospiNotificationWorker.KEY_NOTIFICATION_TYPE to notificationType
        )

        // 7. PeriodicWorkRequest 생성 (24시간 주기)
        val workRequest = PeriodicWorkRequestBuilder<DailyKospiNotificationWorker>(
            1,              // 반복 간격: 1일
            TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        // 8. WorkManager에 작업 등록
        // KEEP: 기존 작업이 있으면 유지 (앱 재시작 시 중복 등록 방지)
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
     * 등록된 모든 일일 알림(09:15, 15:15)을 취소합니다.
     * 사용자가 설정에서 일일 알림을 비활성화할 때 호출됩니다.
     *
     * @param context Context 인스턴스
     */
    fun stopDailyKospiNotifications(context: Context) {
        Timber.d("stopDailyKospiNotifications: 매일 알림 중지")
        WorkManager.getInstance(context).cancelUniqueWork(DailyKospiNotificationWorker.WORK_NAME_MORNING)
        WorkManager.getInstance(context).cancelUniqueWork(DailyKospiNotificationWorker.WORK_NAME_CLOSING)
        Timber.i("stopDailyKospiNotifications: 매일 알림 중지 완료")
    }
}
