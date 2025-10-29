package kim.yeonghoon.kospialarm.domain.usecase

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * GetAlarmsUseCase 단위 테스트.
 *
 * 레포지토리로부터 알람 조회를 테스트합니다.
 */
class GetAlarmsUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var getAlarmsUseCase: GetAlarmsUseCase

    @Before
    fun setUp() {
        alarmRepository = mockk()
        getAlarmsUseCase = GetAlarmsUseCase(alarmRepository)
    }

    @Test
    fun `given repository returns alarms, when invoke is called, then should return alarm list`() = runTest {
        // 준비
        val expectedAlarms = listOf(
            Alarm(
                id = 1,
                baseValue = 2500.0,
                percentage = 5,
                type = AlarmType.RISE,
                isEnabled = true,
                createdAt = System.currentTimeMillis(),
            ),
            Alarm(
                id = 2,
                baseValue = 2500.0,
                percentage = 10,
                type = AlarmType.FALL,
                isEnabled = true,
                createdAt = System.currentTimeMillis(),
            ),
        )
        every { alarmRepository.getAllAlarms() } returns flowOf(expectedAlarms)

        // 실행
        val result = getAlarmsUseCase().first()

        // 검증
        assertEquals(expectedAlarms, result)
        verify(exactly = 1) { alarmRepository.getAllAlarms() }
    }

    @Test
    fun `given repository returns empty list, when invoke is called, then should return empty list`() = runTest {
        // 준비
        every { alarmRepository.getAllAlarms() } returns flowOf(emptyList())

        // 실행
        val result = getAlarmsUseCase().first()

        // 검증
        assertEquals(emptyList<Alarm>(), result)
        verify(exactly = 1) { alarmRepository.getAllAlarms() }
    }

    @Test
    fun `given repository returns multiple alarms, when invoke is called, then should return all alarms in order`() = runTest {
        // 준비
        val alarm1 = Alarm(1, 2500.0, 5, AlarmType.RISE, true, 1000L)
        val alarm2 = Alarm(2, 2500.0, 10, AlarmType.FALL, true, 2000L)
        val alarm3 = Alarm(3, 2500.0, 15, AlarmType.RISE, false, 3000L)
        val expectedAlarms = listOf(alarm1, alarm2, alarm3)
        every { alarmRepository.getAllAlarms() } returns flowOf(expectedAlarms)

        // 실행
        val result = getAlarmsUseCase().first()

        // 검증
        assertEquals(3, result.size)
        assertEquals(expectedAlarms, result)
        verify(exactly = 1) { alarmRepository.getAllAlarms() }
    }
}
