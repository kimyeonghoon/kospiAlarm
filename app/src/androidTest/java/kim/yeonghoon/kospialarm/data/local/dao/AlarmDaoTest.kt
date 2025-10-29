package kim.yeonghoon.kospialarm.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kim.yeonghoon.kospialarm.data.local.AppDatabase
import kim.yeonghoon.kospialarm.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AlarmDao 통합 테스트.
 *
 * 인메모리 데이터베이스를 사용한 Room 데이터베이스 작업을 테스트합니다.
 */
@RunWith(AndroidJUnit4::class)
class AlarmDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var alarmDao: AlarmDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        alarmDao = database.alarmDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAlarm_shouldReturnId() = runTest {
        // 준비
        val alarm = AlarmEntity(
            baseValue = 2500.0,
            percentage = 5,
            type = "RISE",
            isEnabled = true,
            createdAt = 1000L
        )

        // 실행
        val id = alarmDao.insertAlarm(alarm)

        // 검증
        assert(id > 0)
    }

    @Test
    fun insertAndGetAlarmById_shouldReturnCorrectAlarm() = runTest {
        // 준비
        val alarm = AlarmEntity(
            baseValue = 2500.0,
            percentage = 5,
            type = "RISE",
            isEnabled = true,
            createdAt = 1000L
        )
        val id = alarmDao.insertAlarm(alarm)

        // 실행
        val retrieved = alarmDao.getAlarmById(id)

        // 검증
        assertEquals(id, retrieved?.id)
        assertEquals(2500.0, retrieved?.baseValue ?: 0.0, 0.001)
        assertEquals(5, retrieved?.percentage)
        assertEquals("RISE", retrieved?.type)
    }

    @Test
    fun getAlarmById_withInvalidId_shouldReturnNull() = runTest {
        // 실행
        val retrieved = alarmDao.getAlarmById(999)

        // 검증
        assertNull(retrieved)
    }

    @Test
    fun getAllAlarms_shouldReturnAllAlarmsInDescendingOrder() = runTest {
        // 준비
        val alarm1 = AlarmEntity(0, 2500.0, 5, "RISE", true, 1000L)
        val alarm2 = AlarmEntity(0, 2500.0, 10, "FALL", true, 2000L)
        val alarm3 = AlarmEntity(0, 2500.0, 15, "RISE", true, 3000L)

        alarmDao.insertAlarm(alarm1)
        alarmDao.insertAlarm(alarm2)
        alarmDao.insertAlarm(alarm3)

        // 실행
        val alarms = alarmDao.getAllAlarms().first()

        // 검증
        assertEquals(3, alarms.size)
        // createdAt 기준 내림차순이어야 함
        assertEquals(3000L, alarms[0].createdAt)
        assertEquals(2000L, alarms[1].createdAt)
        assertEquals(1000L, alarms[2].createdAt)
    }

    @Test
    fun getAllAlarms_whenEmpty_shouldReturnEmptyList() = runTest {
        // 실행
        val alarms = alarmDao.getAllAlarms().first()

        // 검증
        assertEquals(0, alarms.size)
    }

    @Test
    fun getEnabledAlarms_shouldReturnOnlyEnabledAlarms() = runTest {
        // 준비
        val enabledAlarm1 = AlarmEntity(0, 2500.0, 5, "RISE", true, 1000L)
        val enabledAlarm2 = AlarmEntity(0, 2500.0, 10, "FALL", true, 2000L)
        val disabledAlarm = AlarmEntity(0, 2500.0, 15, "RISE", false, 3000L)

        alarmDao.insertAlarm(enabledAlarm1)
        alarmDao.insertAlarm(enabledAlarm2)
        alarmDao.insertAlarm(disabledAlarm)

        // 실행
        val enabledAlarms = alarmDao.getEnabledAlarms()

        // 검증
        assertEquals(2, enabledAlarms.size)
        assert(enabledAlarms.all { it.isEnabled })
    }

    @Test
    fun getEnabledAlarms_whenNoneEnabled_shouldReturnEmptyList() = runTest {
        // 준비
        val disabledAlarm1 = AlarmEntity(0, 2500.0, 5, "RISE", false, 1000L)
        val disabledAlarm2 = AlarmEntity(0, 2500.0, 10, "FALL", false, 2000L)

        alarmDao.insertAlarm(disabledAlarm1)
        alarmDao.insertAlarm(disabledAlarm2)

        // 실행
        val enabledAlarms = alarmDao.getEnabledAlarms()

        // 검증
        assertEquals(0, enabledAlarms.size)
    }

    @Test
    fun deleteAlarm_shouldRemoveAlarmFromDatabase() = runTest {
        // 준비
        val alarm = AlarmEntity(0, 2500.0, 5, "RISE", true, 1000L)
        val id = alarmDao.insertAlarm(alarm)
        val inserted = alarmDao.getAlarmById(id)!!

        // 실행
        alarmDao.deleteAlarm(inserted)

        // 검증
        val retrieved = alarmDao.getAlarmById(id)
        assertNull(retrieved)
    }

    @Test
    fun updateAlarmEnabled_shouldUpdateEnabledStatus() = runTest {
        // 준비
        val alarm = AlarmEntity(0, 2500.0, 5, "RISE", true, 1000L)
        val id = alarmDao.insertAlarm(alarm)

        // 실행
        alarmDao.updateAlarmEnabled(id, false)

        // 검증
        val updated = alarmDao.getAlarmById(id)
        assertEquals(false, updated?.isEnabled)
    }

    @Test
    fun updateAlarmEnabled_toEnabled_shouldUpdateStatus() = runTest {
        // 준비
        val alarm = AlarmEntity(0, 2500.0, 5, "RISE", false, 1000L)
        val id = alarmDao.insertAlarm(alarm)

        // 실행
        alarmDao.updateAlarmEnabled(id, true)

        // 검증
        val updated = alarmDao.getAlarmById(id)
        assertEquals(true, updated?.isEnabled)
    }

    @Test
    fun insertAlarm_withSameId_shouldReplace() = runTest {
        // 준비
        val alarm1 = AlarmEntity(1, 2500.0, 5, "RISE", true, 1000L)
        val alarm2 = AlarmEntity(1, 2600.0, 10, "FALL", false, 2000L)

        // 실행
        alarmDao.insertAlarm(alarm1)
        alarmDao.insertAlarm(alarm2)

        // 검증
        val retrieved = alarmDao.getAlarmById(1)
        assertEquals(2600.0, retrieved?.baseValue ?: 0.0, 0.001)
        assertEquals(10, retrieved?.percentage)
        assertEquals("FALL", retrieved?.type)
    }
}
