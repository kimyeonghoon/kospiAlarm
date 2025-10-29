package kim.yeonghoon.kospialarm.data.repository

import kim.yeonghoon.kospialarm.data.local.dao.AlarmDao
import kim.yeonghoon.kospialarm.data.local.dao.AlarmHistoryDao
import kim.yeonghoon.kospialarm.data.local.entity.AlarmEntity
import kim.yeonghoon.kospialarm.data.local.entity.AlarmHistoryEntity
import kim.yeonghoon.kospialarm.data.remote.api.KospiApiService
import kim.yeonghoon.kospialarm.data.remote.dto.KospiResponse
import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmHistory
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AlarmRepositoryImpl 단위 테스트.
 *
 * 모의 DAO 및 API 서비스를 사용한 레포지토리 구현을 테스트합니다.
 */
class AlarmRepositoryImplTest {

    private lateinit var alarmDao: AlarmDao
    private lateinit var alarmHistoryDao: AlarmHistoryDao
    private lateinit var kospiApiService: KospiApiService
    private lateinit var repository: AlarmRepositoryImpl

    @Before
    fun setUp() {
        alarmDao = mockk()
        alarmHistoryDao = mockk()
        kospiApiService = mockk()
        repository = AlarmRepositoryImpl(alarmDao, alarmHistoryDao, kospiApiService)
    }

    @Test
    fun `given DAO returns alarms, when getAllAlarms is called, then should return alarm list`() = runTest {
        // 준비
        val alarmEntities = listOf(
            AlarmEntity(1, 2500.0, 5, "RISE", true, 1000L),
            AlarmEntity(2, 2500.0, 10, "FALL", true, 2000L),
        )
        every { alarmDao.getAllAlarms() } returns flowOf(alarmEntities)

        // 실행
        val result = repository.getAllAlarms().first()

        // 검증
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(5, result[0].percentage)
        assertEquals(AlarmType.RISE, result[0].type)
        verify(exactly = 1) { alarmDao.getAllAlarms() }
    }

    @Test
    fun `given DAO returns empty list, when getAllAlarms is called, then should return empty list`() = runTest {
        // 준비
        every { alarmDao.getAllAlarms() } returns flowOf(emptyList())

        // 실행
        val result = repository.getAllAlarms().first()

        // 검증
        assertEquals(0, result.size)
        verify(exactly = 1) { alarmDao.getAllAlarms() }
    }

    @Test
    fun `given valid id, when getAlarmById is called, then should return alarm`() = runTest {
        // 준비
        val alarmEntity = AlarmEntity(1, 2500.0, 5, "RISE", true, 1000L)
        coEvery { alarmDao.getAlarmById(1) } returns alarmEntity

        // 실행
        val result = repository.getAlarmById(1)

        // 검증
        assertEquals(1L, result?.id)
        assertEquals(5, result?.percentage)
        coVerify(exactly = 1) { alarmDao.getAlarmById(1) }
    }

    @Test
    fun `given invalid id, when getAlarmById is called, then should return null`() = runTest {
        // 준비
        coEvery { alarmDao.getAlarmById(999) } returns null

        // 실행
        val result = repository.getAlarmById(999)

        // 검증
        assertNull(result)
        coVerify(exactly = 1) { alarmDao.getAlarmById(999) }
    }

    @Test
    fun `given valid alarm, when createAlarm is called, then should insert and return id`() = runTest {
        // 준비
        val alarm = Alarm(0, 2500.0, 5, AlarmType.RISE, true, 1000L)
        val expectedId = 1L
        coEvery { alarmDao.insertAlarm(any()) } returns expectedId

        // 실행
        val result = repository.createAlarm(alarm)

        // 검증
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { alarmDao.insertAlarm(any()) }
    }

    @Test
    fun `given valid alarm, when deleteAlarm is called, then should delete from DAO`() = runTest {
        // 준비
        val alarm = Alarm(1, 2500.0, 5, AlarmType.RISE, true, 1000L)
        coEvery { alarmDao.deleteAlarm(any()) } just runs

        // 실행
        repository.deleteAlarm(alarm)

        // 검증
        coVerify(exactly = 1) { alarmDao.deleteAlarm(any()) }
    }

    @Test
    fun `given valid alarm id and enabled state, when toggleAlarm is called, then should update DAO`() = runTest {
        // 준비
        val alarmId = 1L
        val isEnabled = false
        coEvery { alarmDao.updateAlarmEnabled(alarmId, isEnabled) } just runs

        // 실행
        repository.toggleAlarm(alarmId, isEnabled)

        // 검증
        coVerify(exactly = 1) { alarmDao.updateAlarmEnabled(alarmId, isEnabled) }
    }

    @Test
    fun `given API returns success, when getCurrentKospiData is called, then should return Success`() = runTest {
        // 준비
        val kospiResponse = KospiResponse(
            index = 2500.5,
            change = 25.5,
            changePercent = 1.03,
            timestamp = System.currentTimeMillis(),
        )
        val response = mockk<retrofit2.Response<KospiResponse>>()
        every { response.isSuccessful } returns true
        every { response.body() } returns kospiResponse
        coEvery { kospiApiService.getCurrentKospi() } returns response

        // 실행
        val result = repository.getCurrentKospiData()

        // 검증
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(2500.5, data.index, 0.001)
        assertEquals(1.03, data.changePercent, 0.001)
        coVerify(exactly = 1) { kospiApiService.getCurrentKospi() }
    }

    @Test
    fun `given API throws exception, when getCurrentKospiData is called, then should return Error`() = runTest {
        // 준비
        val exception = Exception("Network error")
        coEvery { kospiApiService.getCurrentKospi() } throws exception

        // 실행
        val result = repository.getCurrentKospiData()

        // 검증
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
        coVerify(exactly = 1) { kospiApiService.getCurrentKospi() }
    }

    @Test
    fun `given valid history, when saveAlarmHistory is called, then should insert to DAO`() = runTest {
        // 준비
        val history = AlarmHistory(
            id = 0,
            alarmId = 1,
            triggeredValue = 2600.0,
            triggeredAt = System.currentTimeMillis(),
            baseValue = 2500.0,
            percentage = 5,
            type = AlarmType.RISE,
        )
        coEvery { alarmHistoryDao.insertHistory(any()) } returns 1L

        // 실행
        repository.saveAlarmHistory(history)

        // 검증
        coVerify(exactly = 1) { alarmHistoryDao.insertHistory(any()) }
    }

    @Test
    fun `given DAO returns history, when getAllHistory is called, then should return history list`() = runTest {
        // 준비
        val historyEntities = listOf(
            AlarmHistoryEntity(1, 1, 2600.0, 1000L, 2500.0, 5, "RISE"),
            AlarmHistoryEntity(2, 1, 2400.0, 2000L, 2500.0, 5, "FALL"),
        )
        every { alarmHistoryDao.getAllHistory() } returns flowOf(historyEntities)

        // 실행
        val result = repository.getAllHistory().first()

        // 검증
        assertEquals(2, result.size)
        assertEquals(2600.0, result[0].triggeredValue, 0.001)
        assertEquals(5, result[0].percentage)
        verify(exactly = 1) { alarmHistoryDao.getAllHistory() }
    }

    @Test
    fun `given DAO returns empty history, when getAllHistory is called, then should return empty list`() = runTest {
        // 준비
        every { alarmHistoryDao.getAllHistory() } returns flowOf(emptyList())

        // 실행
        val result = repository.getAllHistory().first()

        // 검증
        assertEquals(0, result.size)
        verify(exactly = 1) { alarmHistoryDao.getAllHistory() }
    }
}
