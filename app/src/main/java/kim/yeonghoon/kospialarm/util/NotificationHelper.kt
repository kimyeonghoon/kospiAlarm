package kim.yeonghoon.kospialarm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kim.yeonghoon.kospialarm.R
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import timber.log.Timber

/**
 * 알림 헬퍼 클래스.
 *
 * Android Notification API를 사용하여 푸시 알림을 생성하고 표시합니다.
 * 이 클래스는 다음 세 가지 유형의 알림을 처리합니다:
 *
 * 1. 알림 트리거 (Alert Triggered)
 *    - 사용자가 설정한 임계값에 도달했을 때
 *    - 예: 코스피 5% 상승, 10% 하락
 *
 * 2. API 실패 (API Failure)
 *    - 15분간 연속으로 코스피 데이터를 가져오지 못했을 때
 *    - 네트워크 오류 또는 API 장애
 *
 * 3. 일일 알림 (Daily KOSPI)
 *    - 매일 정해진 시간에 현재 코스피 지수 안내
 *    - 09:15 (장 시작), 15:15 (장 마감 임박)
 *
 * 알림 설정:
 * - 채널: KOSPI_ALERTS
 * - 우선순위: HIGH (헤드업 알림)
 * - 소리: 없음
 * - 진동: 있음 (패턴: 0-500-200-500ms)
 */
object NotificationHelper {

    /** 알림 채널 ID (Android 8.0+ 필수) */
    private const val CHANNEL_ID = "KOSPI_ALERTS"

    /** 알림 채널 이름 (사용자에게 표시됨) */
    private const val CHANNEL_NAME = "KOSPI Alerts"

    /** 알림 트리거 알림 ID 기준값 (실제 ID는 alarmId를 더해서 사용) */
    private const val ALERT_NOTIFICATION_ID = 1000

    /** API 실패 알림 ID */
    private const val API_FAILURE_NOTIFICATION_ID = 2000

    /** 일일 코스피 알림 ID */
    private const val DAILY_KOSPI_NOTIFICATION_ID = 3000

    /**
     * 알림 채널 생성.
     *
     * Android 8.0 (Oreo, API 26) 이상에서 필수입니다.
     * 채널이 없으면 알림이 표시되지 않습니다.
     *
     * 채널 설정:
     * - 중요도: HIGH (헤드업 알림, 상태바 아이콘 표시)
     * - 진동: 활성화 (패턴: 0-500-200-500ms)
     * - 소리: 없음
     *
     * 참고:
     * - 같은 ID로 여러 번 호출해도 기존 채널이 덮어씌워지지 않습니다.
     * - 사용자가 설정에서 채널 설정을 변경하면 코드 변경은 무시됩니다.
     *
     * @param context Context 인스턴스
     */
    fun createNotificationChannel(context: Context) {
        Timber.d("createNotificationChannel: 알림 채널 생성 시작")

        // Android 8.0 이상에서만 채널 생성 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH  // 헤드업 알림용
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "코스피 알림을 위한 채널"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)  // 진동-멈춤-진동
                setSound(null, null)  // 소리 없음 (진동만)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Timber.i("createNotificationChannel: 알림 채널 생성 완료")
        }
    }

    /**
     * 알림 트리거 시 알림 표시.
     *
     * 사용자가 설정한 임계값(예: 5%, 10%)에 코스피 지수가 도달했을 때 호출됩니다.
     *
     * 알림 내용:
     * - 제목: "코스피 {percentage}% {상승/하락} 알림"
     * - 내용: "기준: {baseValue} → 현재: {currentValue}"
     *
     * 알림 ID:
     * - ALERT_NOTIFICATION_ID + alarmId를 사용하여 각 알림마다 고유 ID 보장
     * - 이를 통해 여러 알림이 동시에 표시될 수 있음
     *
     * @param context Context 인스턴스
     * @param alarmId 알림 데이터베이스 ID
     * @param baseValue 기준 값 (알림 생성 시점의 코스피 지수)
     * @param currentValue 현재 값 (임계값 도달 시점의 코스피 지수)
     * @param percentage 퍼센트 (5, 10 등)
     * @param type 알림 타입 (RISE: 상승, FALL: 하락)
     */
    fun showAlarmTriggeredNotification(
        context: Context,
        alarmId: Long,
        baseValue: Double,
        currentValue: Double,
        percentage: Int,
        type: AlarmType
    ) {
        Timber.d("showAlarmTriggeredNotification: alarmId=$alarmId")

        val typeText = if (type == AlarmType.RISE) "상승" else "하락"
        val title = "코스피 ${percentage}% $typeText 알림"
        val content = "기준: ${String.format("%.2f", baseValue)} → 현재: ${String.format("%.2f", currentValue)}"

        // TODO: 실제 Activity로 변경
        val intent = Intent() // Intent(context, AlarmDetailActivity::class.java)
        intent.putExtra("alarm_id", alarmId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stock_chart)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID + alarmId.toInt(), notification)

        Timber.i("showAlarmTriggeredNotification: 알림 표시 완료")
    }

    /**
     * API 실패 알림 표시.
     *
     * 15분간 연속으로 코스피 데이터를 가져오지 못했을 때 호출됩니다.
     * KospiCheckWorker에서 3회 연속 실패 시 호출됩니다.
     *
     * 알림 내용:
     * - 제목: "코스피 데이터 조회 실패"
     * - 내용: "15분간 데이터를 가져올 수 없습니다. 네트워크를 확인해주세요."
     *
     * 참고:
     * - 고정 ID(API_FAILURE_NOTIFICATION_ID)를 사용하여 중복 알림 방지
     * - 사용자가 문제를 인지할 수 있도록 HIGH 우선순위 사용
     *
     * @param context Context 인스턴스
     */
    fun showApiFailureNotification(context: Context) {
        Timber.d("showApiFailureNotification: API 실패 알림 표시 시작")

        val title = "코스피 데이터 조회 실패"
        val content = "15분간 데이터를 가져올 수 없습니다. 네트워크를 확인해주세요."

        // TODO: 실제 Activity로 변경
        val intent = Intent() // Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stock_chart)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(API_FAILURE_NOTIFICATION_ID, notification)

        Timber.i("showApiFailureNotification: API 실패 알림 표시 완료")
    }

    /**
     * 매일 정해진 시간에 KOSPI 지수 알림 표시.
     *
     * 09:15 (장 시작)와 15:15 (장 마감 임박)에 현재 코스피 지수를 알려줍니다.
     * DailyKospiNotificationWorker에서 호출됩니다.
     *
     * 알림 내용:
     * - 제목: Worker에서 전달 (예: "장 시작 - 코스피 지수")
     * - 내용: Worker에서 전달 (예: "현재 코스피: 2,500.00 (+1.5%)")
     *
     * 참고:
     * - 고정 ID(DAILY_KOSPI_NOTIFICATION_ID)를 사용하여 최신 알림만 유지
     * - 이전 일일 알림은 자동으로 대체됨
     *
     * @param context Context 인스턴스
     * @param title 알림 제목 (Worker에서 전달)
     * @param message 알림 내용 (Worker에서 전달)
     */
    fun showDailyKospiNotification(
        context: Context,
        title: String,
        message: String
    ) {
        Timber.d("showDailyKospiNotification: 매일 알림 표시 시작")

        // TODO: 실제 Activity로 변경
        val intent = Intent() // Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stock_chart)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DAILY_KOSPI_NOTIFICATION_ID, notification)

        Timber.i("showDailyKospiNotification: 매일 알림 표시 완료")
    }
}
