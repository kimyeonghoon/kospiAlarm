package kim.yeonghoon.kospialarm.presentation.viewmodel

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import kim.yeonghoon.kospialarm.domain.model.KospiData
import kim.yeonghoon.kospialarm.domain.usecase.CreateAlarmUseCase
import kim.yeonghoon.kospialarm.domain.usecase.DeleteAlarmUseCase
import kim.yeonghoon.kospialarm.domain.usecase.GetAlarmsUseCase
import kim.yeonghoon.kospialarm.domain.usecase.GetCurrentKospiUseCase
import kim.yeonghoon.kospialarm.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AlarmListViewModel 단위 테스트.
 *
 * ViewModel 상태 관리 및 UseCase 상호작용을 테스트합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmListViewModelTest {

    private lateinit var getAlarmsUseCase: GetAlarmsUseCase
    private lateinit var createAlarmUseCase: CreateAlarmUseCase
    private lateinit var deleteAlarmUseCase: DeleteAlarmUseCase
    private lateinit var getCurrentKospiUseCase: GetCurrentKospiUseCase
    private lateinit var viewModel: AlarmListViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAlarmsUseCase = mockk()
        createAlarmUseCase = mockk()
        deleteAlarmUseCase = mockk()
        getCurrentKospiUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given UseCases return success, when ViewModel is initialized, then should load alarms and KOSPI data`() = runTest {
        // 준비
        val alarms = listOf(
            Alarm(1, 2500.0, 5, AlarmType.RISE, true, 1000L),
            Alarm(2, 2500.0, 10, AlarmType.FALL, true, 2000L),
        )
        val kospiData = KospiData(2500.0, System.currentTimeMillis(), 25.0, 1.0)
        every { getAlarmsUseCase() } returns flowOf(alarms)
        coEvery { getCurrentKospiUseCase() } returns Result.Success(kospiData)

        // 실행
        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 검증
        assertTrue(viewModel.uiState.value is AlarmListUiState.Success)
        val state = viewModel.uiState.value as AlarmListUiState.Success
        assertEquals(2, state.alarms.size)
        assertEquals(kospiData, viewModel.kospiData.value)
        verify(exactly = 1) { getAlarmsUseCase() }
        coVerify(exactly = 1) { getCurrentKospiUseCase() }
    }

    @Test
    fun `given GetAlarmsUseCase returns empty list, when ViewModel is initialized, then should show empty Success state`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returns Result.Success(
            KospiData(2500.0, System.currentTimeMillis(), 0.0, 0.0)
        )

        // 실행
        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 검증
        assertTrue(viewModel.uiState.value is AlarmListUiState.Success)
        val state = viewModel.uiState.value as AlarmListUiState.Success
        assertEquals(0, state.alarms.size)
    }

    @Test
    fun `given GetCurrentKospiUseCase returns error, when ViewModel is initialized, then should set kospiData to null`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returns Result.Error(Exception("Network error"))

        // 실행
        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 검증
        assertNull(viewModel.kospiData.value)
        coVerify(exactly = 1) { getCurrentKospiUseCase() }
    }

    @Test
    fun `given valid alarm, when createAlarm is called, then should call CreateAlarmUseCase`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returns Result.Success(
            KospiData(2500.0, System.currentTimeMillis(), 0.0, 0.0)
        )
        val alarm = Alarm(0, 2500.0, 5, AlarmType.RISE, true, 1000L)
        coEvery { createAlarmUseCase(alarm) } returns 1L

        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 실행
        viewModel.createAlarm(alarm)

        // 검증
        coVerify(exactly = 1) { createAlarmUseCase(alarm) }
    }

    @Test
    fun `given createAlarmUseCase throws exception, when createAlarm is called, then should handle error gracefully`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returns Result.Success(
            KospiData(2500.0, System.currentTimeMillis(), 0.0, 0.0)
        )
        val alarm = Alarm(0, 2500.0, 5, AlarmType.RISE, true, 1000L)
        coEvery { createAlarmUseCase(alarm) } throws Exception("DB error")

        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 실행
        viewModel.createAlarm(alarm)

        // 검증 - 예외가 발생하지 않고 오류가 처리됨
        coVerify(exactly = 1) { createAlarmUseCase(alarm) }
    }

    @Test
    fun `given valid alarm, when deleteAlarm is called, then should call DeleteAlarmUseCase`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returns Result.Success(
            KospiData(2500.0, System.currentTimeMillis(), 0.0, 0.0)
        )
        val alarm = Alarm(1, 2500.0, 5, AlarmType.RISE, true, 1000L)
        coEvery { deleteAlarmUseCase(alarm) } just runs

        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 실행
        viewModel.deleteAlarm(alarm)

        // 검증
        coVerify(exactly = 1) { deleteAlarmUseCase(alarm) }
    }

    @Test
    fun `given deleteAlarmUseCase throws exception, when deleteAlarm is called, then should handle error gracefully`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returns Result.Success(
            KospiData(2500.0, System.currentTimeMillis(), 0.0, 0.0)
        )
        val alarm = Alarm(1, 2500.0, 5, AlarmType.RISE, true, 1000L)
        coEvery { deleteAlarmUseCase(alarm) } throws Exception("DB error")

        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 실행
        viewModel.deleteAlarm(alarm)

        // 검증 - 예외가 발생하지 않고 오류가 처리됨
        coVerify(exactly = 1) { deleteAlarmUseCase(alarm) }
    }

    @Test
    fun `given UseCases ready, when loadKospiData is called, then should reload KOSPI data`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        val kospiData1 = KospiData(2500.0, 1000L, 0.0, 0.0)
        val kospiData2 = KospiData(2550.0, 2000L, 50.0, 2.0)
        coEvery { getCurrentKospiUseCase() } returnsMany listOf(
            Result.Success(kospiData1),
            Result.Success(kospiData2)
        )

        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        // 실행
        viewModel.loadKospiData()

        // 검증
        assertEquals(kospiData2, viewModel.kospiData.value)
        coVerify(exactly = 2) { getCurrentKospiUseCase() }
    }

    @Test
    fun `given GetCurrentKospiUseCase returns Loading, when loadKospiData is called, then should not update kospiData`() = runTest {
        // 준비
        every { getAlarmsUseCase() } returns flowOf(emptyList())
        coEvery { getCurrentKospiUseCase() } returnsMany listOf(
            Result.Loading,
            Result.Loading
        )

        viewModel = AlarmListViewModel(
            getAlarmsUseCase,
            createAlarmUseCase,
            deleteAlarmUseCase,
            getCurrentKospiUseCase
        )

        val kospiDataBefore = viewModel.kospiData.value

        // 실행
        viewModel.loadKospiData()

        // 검증
        assertEquals(kospiDataBefore, viewModel.kospiData.value)
        coVerify(exactly = 2) { getCurrentKospiUseCase() }
    }
}
