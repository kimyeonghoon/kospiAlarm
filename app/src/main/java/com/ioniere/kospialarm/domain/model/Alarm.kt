package com.ioniere.kospialarm.domain.model

/**
 * 알림 도메인 모델.
 *
 * @property id 알림 고유 ID
 * @property baseValue 알림 설정 시 기준 코스피 값
 * @property percentage 알림 퍼센트 (5, 10, 15, 20)
 * @property type 알림 타입 (상승/하락)
 * @property isEnabled 알림 활성화 여부
 * @property createdAt 알림 생성 시간
 */
data class Alarm(
    val id: Long = 0,
    val baseValue: Double,
    val percentage: Int,
    val type: AlarmType,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
