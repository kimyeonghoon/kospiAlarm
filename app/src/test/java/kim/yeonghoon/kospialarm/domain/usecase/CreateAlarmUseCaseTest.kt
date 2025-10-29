package kim.yeonghoon.kospialarm.domain.usecase

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * CreateAlarmUseCase 단위 테스트.
 *
 * 레포지토리를 통한 새 알람 생성을 테스트합니다.
 */
class CreateAlarmUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var createAlarmUseCase: CreateAlarmUseCase

    @Before
    fun setUp() {
        alarmRepository = mockk()
        createAlarmUseCase = CreateAlarmUseCase(alarmRepository)
    }

    @Test
    fun `given valid alarm data, when invoke is called, then should create alarm and return id`() = runTest {
        // 준비
        val alarm = Alarm(
            id = 0,
            baseValue = 2500.0,
            percentage = 5,
            type = AlarmType.RISE,
            isEnabled = true,
            createdAt = System.currentTimeMillis(),
        )
        val expectedId = 1L
        coEvery { alarmRepository.createAlarm(alarm) } returns expectedId

        // 실행
        val result = createAlarmUseCase(alarm)

        // 검증
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { alarmRepository.createAlarm(alarm) }
    }

    @Test
    fun `given alarm with RISE type, when invoke is called, then should create RISE alarm`() = runTest {
        // 준비
        val alarm = Alarm(
            id = 0,
            baseValue = 2600.0,
            percentage = 10,
            type = AlarmType.RISE,
            isEnabled = true,
            createdAt = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.createAlarm(alarm) } returns 2L

        // 실행
        val result = createAlarmUseCase(alarm)

        // 검증
        assertEquals(2L, result)
        coVerify(exactly = 1) {
            alarmRepository.createAlarm(match { it.type == AlarmType.RISE })
        }
    }

    @Test
    fun `given alarm with FALL type, when invoke is called, then should create FALL alarm`() = runTest {
        // 준비
        val alarm = Alarm(
            id = 0,
            baseValue = 2400.0,
            percentage = 15,
            type = AlarmType.FALL,
            isEnabled = true,
            createdAt = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.createAlarm(alarm) } returns 3L

        // 실행
        val result = createAlarmUseCase(alarm)

        // 검증
        assertEquals(3L, result)
        coVerify(exactly = 1) {
            alarmRepository.createAlarm(match { it.type == AlarmType.FALL })
        }
    }

    @Test
    fun `given disabled alarm, when invoke is called, then should create disabled alarm`() = runTest {
        // 준비
        val alarm = Alarm(
            id = 0,
            baseValue = 2500.0,
            percentage = 20,
            type = AlarmType.RISE,
            isEnabled = false,
            createdAt = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.createAlarm(alarm) } returns 4L

        // 실행
        val result = createAlarmUseCase(alarm)

        // 검증
        assertEquals(4L, result)
        coVerify(exactly = 1) {
            alarmRepository.createAlarm(match { !it.isEnabled })
        }
    }
}
