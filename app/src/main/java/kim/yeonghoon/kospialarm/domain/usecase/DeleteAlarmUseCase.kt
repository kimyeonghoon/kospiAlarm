package kim.yeonghoon.kospialarm.domain.usecase

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * 알림을 삭제하는 유스케이스.
 *
 * @property repository 알림 레포지토리
 */
class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    /**
     * 알림을 삭제.
     *
     * @param alarm 삭제할 알림
     */
    suspend operator fun invoke(alarm: Alarm) {
        repository.deleteAlarm(alarm)
    }
}
