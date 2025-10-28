package com.ioniere.kospialarm.domain.model

/**
 * 알림 히스토리 도메인 모델.
 *
 * @property id 히스토리 고유 ID
 * @property alarmId 관련 알림 ID
 * @property triggeredValue 알림 발생 시 코스피 값
 * @property triggeredAt 알림 발생 시간
 * @property baseValue 알림 설정 시 기준 값
 * @property percentage 알림 퍼센트
 * @property type 알림 타입
 */
data class AlarmHistory(
    val id: Long = 0,
    val alarmId: Long,
    val triggeredValue: Double,
    val triggeredAt: Long,
    val baseValue: Double,
    val percentage: Int,
    val type: AlarmType
)
