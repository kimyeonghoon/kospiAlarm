package kim.yeonghoon.kospialarm.domain.usecase

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * DeleteAlarmUseCase 단위 테스트.
 *
 * 레포지토리를 통한 알람 삭제를 테스트합니다.
 */
class DeleteAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var deleteAlarmUseCase: DeleteAlarmUseCase

    @Before
    fun setUp() {
        alarmRepository = mockk()
        deleteAlarmUseCase = DeleteAlarmUseCase(alarmRepository)
    }

    @Test
    fun `given valid alarm, when invoke is called, then should delete alarm`() = runTest {
        // 준비
        val alarm = Alarm(
            id = 1,
            baseValue = 2500.0,
            percentage = 5,
            type = AlarmType.RISE,
            isEnabled = true,
            createdAt = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.deleteAlarm(alarm) } just runs

        // 실행
        deleteAlarmUseCase(alarm)

        // 검증
        coVerify(exactly = 1) { alarmRepository.deleteAlarm(alarm) }
    }

    @Test
    fun `given alarm with id, when invoke is called, then should delete correct alarm`() = runTest {
        // 준비
        val alarmId = 123L
        val alarm = Alarm(
            id = alarmId,
            baseValue = 2500.0,
            percentage = 10,
            type = AlarmType.FALL,
            isEnabled = true,
            createdAt = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.deleteAlarm(alarm) } just runs

        // 실행
        deleteAlarmUseCase(alarm)

        // 검증
        coVerify(exactly = 1) {
            alarmRepository.deleteAlarm(match { it.id == alarmId })
        }
    }

    @Test
    fun `given disabled alarm, when invoke is called, then should delete alarm regardless of enabled state`() = runTest {
        // 준비
        val alarm = Alarm(
            id = 2,
            baseValue = 2500.0,
            percentage = 15,
            type = AlarmType.RISE,
            isEnabled = false,
            createdAt = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.deleteAlarm(alarm) } just runs

        // 실행
        deleteAlarmUseCase(alarm)

        // 검증
        coVerify(exactly = 1) { alarmRepository.deleteAlarm(alarm) }
    }
}
