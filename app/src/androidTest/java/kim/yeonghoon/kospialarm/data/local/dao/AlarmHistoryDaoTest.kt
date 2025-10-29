package kim.yeonghoon.kospialarm.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kim.yeonghoon.kospialarm.data.local.AppDatabase
import kim.yeonghoon.kospialarm.data.local.entity.AlarmHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AlarmHistoryDao 통합 테스트.
 *
 * 인메모리 데이터베이스를 사용한 Room 데이터베이스 작업을 테스트합니다.
 */
@RunWith(AndroidJUnit4::class)
class AlarmHistoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var historyDao: AlarmHistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        historyDao = database.alarmHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertHistory_shouldReturnId() = runTest {
        // 준비
        val history = AlarmHistoryEntity(
            alarmId = 1,
            triggeredValue = 2625.0,
            triggeredAt = 1000L,
            baseValue = 2500.0,
            percentage = 5,
            type = "RISE"
        )

        // 실행
        val id = historyDao.insertHistory(history)

        // 검증
        assert(id > 0)
    }

    @Test
    fun getAllHistory_shouldReturnAllHistoryInDescendingOrder() = runTest {
        // 준비
        val history1 = AlarmHistoryEntity(0, 1, 2625.0, 1000L, 2500.0, 5, "RISE")
        val history2 = AlarmHistoryEntity(0, 1, 2375.0, 2000L, 2500.0, 5, "FALL")
        val history3 = AlarmHistoryEntity(0, 2, 2750.0, 3000L, 2500.0, 10, "RISE")

        historyDao.insertHistory(history1)
        historyDao.insertHistory(history2)
        historyDao.insertHistory(history3)

        // 실행
        val histories = historyDao.getAllHistory().first()

        // 검증
        assertEquals(3, histories.size)
        // triggeredAt 기준 내림차순이어야 함
        assertEquals(3000L, histories[0].triggeredAt)
        assertEquals(2000L, histories[1].triggeredAt)
        assertEquals(1000L, histories[2].triggeredAt)
    }

    @Test
    fun getAllHistory_whenEmpty_shouldReturnEmptyList() = runTest {
        // 실행
        val histories = historyDao.getAllHistory().first()

        // 검증
        assertEquals(0, histories.size)
    }

    @Test
    fun getHistoryByAlarmId_shouldReturnOnlyMatchingHistory() = runTest {
        // 준비
        val history1 = AlarmHistoryEntity(0, 1, 2625.0, 1000L, 2500.0, 5, "RISE")
        val history2 = AlarmHistoryEntity(0, 1, 2750.0, 2000L, 2500.0, 10, "RISE")
        val history3 = AlarmHistoryEntity(0, 2, 2375.0, 3000L, 2500.0, 5, "FALL")

        historyDao.insertHistory(history1)
        historyDao.insertHistory(history2)
        historyDao.insertHistory(history3)

        // 실행
        val alarm1Histories = historyDao.getHistoryByAlarmId(1)

        // 검증
        assertEquals(2, alarm1Histories.size)
        assert(alarm1Histories.all { it.alarmId == 1L })
    }

    @Test
    fun getHistoryByAlarmId_withNoMatches_shouldReturnEmptyList() = runTest {
        // 준비
        val history = AlarmHistoryEntity(0, 1, 2625.0, 1000L, 2500.0, 5, "RISE")
        historyDao.insertHistory(history)

        // 실행
        val histories = historyDao.getHistoryByAlarmId(999)

        // 검증
        assertEquals(0, histories.size)
    }

    @Test
    fun insertMultipleHistories_shouldPreserveAllData() = runTest {
        // 준비
        val histories = listOf(
            AlarmHistoryEntity(0, 1, 2625.0, 1000L, 2500.0, 5, "RISE"),
            AlarmHistoryEntity(0, 1, 2750.0, 2000L, 2500.0, 10, "RISE"),
            AlarmHistoryEntity(0, 2, 2375.0, 3000L, 2500.0, 5, "FALL"),
            AlarmHistoryEntity(0, 2, 2250.0, 4000L, 2500.0, 10, "FALL")
        )

        // 실행
        histories.forEach { historyDao.insertHistory(it) }

        // 검증
        val allHistories = historyDao.getAllHistory().first()
        assertEquals(4, allHistories.size)
    }

    @Test
    fun insertHistory_shouldPreserveAllFields() = runTest {
        // 준비
        val history = AlarmHistoryEntity(
            id = 0,
            alarmId = 123,
            triggeredValue = 2625.5,
            triggeredAt = 5000L,
            baseValue = 2500.5,
            percentage = 5,
            type = "RISE"
        )

        // 실행
        val id = historyDao.insertHistory(history)
        val retrieved = historyDao.getAllHistory().first().first()

        // 검증
        assertEquals(id, retrieved.id)
        assertEquals(123L, retrieved.alarmId)
        assertEquals(2625.5, retrieved.triggeredValue, 0.001)
        assertEquals(5000L, retrieved.triggeredAt)
        assertEquals(2500.5, retrieved.baseValue, 0.001)
        assertEquals(5, retrieved.percentage)
        assertEquals("RISE", retrieved.type)
    }
}
