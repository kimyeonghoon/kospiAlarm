package com.ioniere.kospialarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ioniere.kospialarm.domain.model.AlarmHistory
import com.ioniere.kospialarm.domain.model.AlarmType

/**
 * 알림 히스토리 Room 엔티티.
 *
 * @property id 히스토리 고유 ID
 * @property alarmId 관련 알림 ID
 * @property triggeredValue 알림 발생 시 코스피 값
 * @property triggeredAt 알림 발생 시간
 * @property baseValue 알림 설정 시 기준 값
 * @property percentage 알림 퍼센트
 * @property type 알림 타입
 */
@Entity(tableName = "alarm_history")
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val triggeredValue: Double,
    val triggeredAt: Long,
    val baseValue: Double,
    val percentage: Int,
    val type: String
) {
    /**
     * 엔티티를 도메인 모델로 변환.
     *
     * @return AlarmHistory 도메인 모델
     */
    fun toDomain(): AlarmHistory {
        return AlarmHistory(
            id = id,
            alarmId = alarmId,
            triggeredValue = triggeredValue,
            triggeredAt = triggeredAt,
            baseValue = baseValue,
            percentage = percentage,
            type = AlarmType.valueOf(type)
        )
    }

    companion object {
        /**
         * 도메인 모델을 엔티티로 변환.
         *
         * @param history 도메인 모델
         * @return AlarmHistoryEntity
         */
        fun fromDomain(history: AlarmHistory): AlarmHistoryEntity {
            return AlarmHistoryEntity(
                id = history.id,
                alarmId = history.alarmId,
                triggeredValue = history.triggeredValue,
                triggeredAt = history.triggeredAt,
                baseValue = history.baseValue,
                percentage = history.percentage,
                type = history.type.name
            )
        }
    }
}
