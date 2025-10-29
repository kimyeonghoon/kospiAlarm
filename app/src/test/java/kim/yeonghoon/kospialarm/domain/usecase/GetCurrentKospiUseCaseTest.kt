package kim.yeonghoon.kospialarm.domain.usecase

import kim.yeonghoon.kospialarm.domain.model.KospiData
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import kim.yeonghoon.kospialarm.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * GetCurrentKospiUseCase 단위 테스트.
 *
 * 레포지토리로부터 현재 KOSPI 데이터 조회를 테스트합니다.
 */
class GetCurrentKospiUseCaseTest {

    private lateinit var alarmRepository: AlarmRepository
    private lateinit var getCurrentKospiUseCase: GetCurrentKospiUseCase

    @Before
    fun setUp() {
        alarmRepository = mockk()
        getCurrentKospiUseCase = GetCurrentKospiUseCase(alarmRepository)
    }

    @Test
    fun `given repository returns success, when invoke is called, then should return success with KOSPI data`() = runTest {
        // 준비
        val expectedKospiData = KospiData(
            index = 2500.5,
            change = 25.5,
            changePercent = 1.03,
            timestamp = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.getCurrentKospiData() } returns Result.Success(expectedKospiData)

        // 실행
        val result = getCurrentKospiUseCase()

        // 검증
        assertTrue(result is Result.Success)
        assertEquals(expectedKospiData, (result as Result.Success).data)
        coVerify(exactly = 1) { alarmRepository.getCurrentKospiData() }
    }

    @Test
    fun `given repository returns error, when invoke is called, then should return error`() = runTest {
        // 준비
        val exception = Exception("Network error")
        coEvery { alarmRepository.getCurrentKospiData() } returns Result.Error(exception)

        // 실행
        val result = getCurrentKospiUseCase()

        // 검증
        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
        coVerify(exactly = 1) { alarmRepository.getCurrentKospiData() }
    }

    @Test
    fun `given repository returns loading, when invoke is called, then should return loading`() = runTest {
        // 준비
        coEvery { alarmRepository.getCurrentKospiData() } returns Result.Loading

        // 실행
        val result = getCurrentKospiUseCase()

        // 검증
        assertTrue(result is Result.Loading)
        coVerify(exactly = 1) { alarmRepository.getCurrentKospiData() }
    }

    @Test
    fun `given KOSPI data with positive change, when invoke is called, then should return correct positive change percent`() = runTest {
        // 준비
        val kospiData = KospiData(
            index = 2600.0,
            change = 100.0,
            changePercent = 4.0,
            timestamp = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.getCurrentKospiData() } returns Result.Success(kospiData)

        // 실행
        val result = getCurrentKospiUseCase()

        // 검증
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(4.0, data.changePercent, 0.001)
        assertTrue(data.changePercent > 0)
        coVerify(exactly = 1) { alarmRepository.getCurrentKospiData() }
    }

    @Test
    fun `given KOSPI data with negative change, when invoke is called, then should return correct negative change percent`() = runTest {
        // 준비
        val kospiData = KospiData(
            index = 2400.0,
            change = -100.0,
            changePercent = -4.0,
            timestamp = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.getCurrentKospiData() } returns Result.Success(kospiData)

        // 실행
        val result = getCurrentKospiUseCase()

        // 검증
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(-4.0, data.changePercent, 0.001)
        assertTrue(data.changePercent < 0)
        coVerify(exactly = 1) { alarmRepository.getCurrentKospiData() }
    }

    @Test
    fun `given KOSPI data with zero change, when invoke is called, then should return zero change percent`() = runTest {
        // 준비
        val kospiData = KospiData(
            index = 2500.0,
            change = 0.0,
            changePercent = 0.0,
            timestamp = System.currentTimeMillis(),
        )
        coEvery { alarmRepository.getCurrentKospiData() } returns Result.Success(kospiData)

        // 실행
        val result = getCurrentKospiUseCase()

        // 검증
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(0.0, data.changePercent, 0.001)
        coVerify(exactly = 1) { alarmRepository.getCurrentKospiData() }
    }
}
