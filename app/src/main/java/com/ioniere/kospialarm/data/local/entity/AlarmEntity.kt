package com.ioniere.kospialarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ioniere.kospialarm.domain.model.Alarm
import com.ioniere.kospialarm.domain.model.AlarmType

/**
 * 알림 Room 엔티티.
 *
 * @property id 알림 고유 ID
 * @property baseValue 알림 설정 시 기준 코스피 값
 * @property percentage 알림 퍼센트
 * @property type 알림 타입 (상승/하락)
 * @property isEnabled 알림 활성화 여부
 * @property createdAt 알림 생성 시간
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val baseValue: Double,
    val percentage: Int,
    val type: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 엔티티를 도메인 모델로 변환.
     *
     * @return Alarm 도메인 모델
     */
    fun toDomain(): Alarm {
        return Alarm(
            id = id,
            baseValue = baseValue,
            percentage = percentage,
            type = AlarmType.valueOf(type),
            isEnabled = isEnabled,
            createdAt = createdAt
        )
    }

    companion object {
        /**
         * 도메인 모델을 엔티티로 변환.
         *
         * @param alarm 도메인 모델
         * @return AlarmEntity
         */
        fun fromDomain(alarm: Alarm): AlarmEntity {
            return AlarmEntity(
                id = alarm.id,
                baseValue = alarm.baseValue,
                percentage = alarm.percentage,
                type = alarm.type.name,
                isEnabled = alarm.isEnabled,
                createdAt = alarm.createdAt
            )
        }
    }
}
