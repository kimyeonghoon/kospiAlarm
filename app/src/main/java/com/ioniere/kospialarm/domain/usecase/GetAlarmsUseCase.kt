package com.ioniere.kospialarm.domain.usecase

import com.ioniere.kospialarm.domain.model.Alarm
import com.ioniere.kospialarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 모든 알림을 조회하는 유스케이스.
 *
 * @property repository 알림 레포지토리
 */
class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    /**
     * 모든 알림을 Flow로 반환.
     *
     * @return 알림 목록 Flow
     */
    operator fun invoke(): Flow<List<Alarm>> {
        return repository.getAllAlarms()
    }
}
