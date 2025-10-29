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
 */
object NotificationHelper {

    private const val CHANNEL_ID = "KOSPI_ALERTS"
    private const val CHANNEL_NAME = "KOSPI Alerts"
    private const val ALERT_NOTIFICATION_ID = 1000
    private const val API_FAILURE_NOTIFICATION_ID = 2000
    private const val DAILY_KOSPI_NOTIFICATION_ID = 3000

    /**
     * 알림 채널 생성.
     *
     * @param context Context
     */
    fun createNotificationChannel(context: Context) {
        Timber.d("createNotificationChannel: 알림 채널 생성 시작")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "코스피 알림을 위한 채널"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(null, null) // 소리 없음
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Timber.i("createNotificationChannel: 알림 채널 생성 완료")
        }
    }

    /**
     * 알림 트리거 시 알림 표시.
     *
     * @param context Context
     * @param alarmId 알림 ID
     * @param baseValue 기준 값
     * @param currentValue 현재 값
     * @param percentage 퍼센트
     * @param type 알림 타입
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
     * @param context Context
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
     * @param context Context
     * @param title 알림 제목
     * @param message 알림 내용
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
