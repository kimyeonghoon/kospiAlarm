package kim.yeonghoon.kospialarm.data.local.dao

import androidx.room.*
import kim.yeonghoon.kospialarm.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * 알림 DAO (Data Access Object).
 */
@Dao
interface AlarmDao {
    /**
     * 모든 알림을 Flow로 조회.
     *
     * @return 알림 목록 Flow
     */
    @Query("SELECT * FROM alarms ORDER BY createdAt DESC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    /**
     * ID로 알림 조회.
     *
     * @param id 알림 ID
     * @return 알림 또는 null
     */
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    /**
     * 활성화된 알림만 조회.
     *
     * @return 활성화된 알림 목록
     */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    /**
     * 알림 추가.
     *
     * @param alarm 추가할 알림
     * @return 추가된 알림 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    /**
     * 알림 삭제.
     *
     * @param alarm 삭제할 알림
     */
    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    /**
     * 알림 활성화/비활성화 업데이트.
     *
     * @param alarmId 알림 ID
     * @param isEnabled 활성화 여부
     */
    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :alarmId")
    suspend fun updateAlarmEnabled(alarmId: Long, isEnabled: Boolean)
}
