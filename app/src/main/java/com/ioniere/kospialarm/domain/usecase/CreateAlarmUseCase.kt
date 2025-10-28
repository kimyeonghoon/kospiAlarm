package com.ioniere.kospialarm.domain.usecase

import com.ioniere.kospialarm.domain.model.Alarm
import com.ioniere.kospialarm.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * 알림을 생성하는 유스케이스.
 *
 * @property repository 알림 레포지토리
 */
class CreateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    /**
     * 새로운 알림을 생성.
     *
     * @param alarm 생성할 알림
     * @return 생성된 알림 ID
     */
    suspend operator fun invoke(alarm: Alarm): Long {
        return repository.createAlarm(alarm)
    }
}
