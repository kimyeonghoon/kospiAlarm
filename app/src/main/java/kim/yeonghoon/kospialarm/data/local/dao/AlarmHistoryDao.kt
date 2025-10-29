package kim.yeonghoon.kospialarm.data.local.dao

import androidx.room.*
import kim.yeonghoon.kospialarm.data.local.entity.AlarmHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 알림 히스토리 DAO (Data Access Object).
 */
@Dao
interface AlarmHistoryDao {
    /**
     * 모든 히스토리를 Flow로 조회.
     *
     * @return 히스토리 목록 Flow
     */
    @Query("SELECT * FROM alarm_history ORDER BY triggeredAt DESC")
    fun getAllHistory(): Flow<List<AlarmHistoryEntity>>

    /**
     * 특정 알림의 히스토리 조회.
     *
     * @param alarmId 알림 ID
     * @return 히스토리 목록
     */
    @Query("SELECT * FROM alarm_history WHERE alarmId = :alarmId ORDER BY triggeredAt DESC")
    suspend fun getHistoryByAlarmId(alarmId: Long): List<AlarmHistoryEntity>

    /**
     * 히스토리 추가.
     *
     * @param history 추가할 히스토리
     * @return 추가된 히스토리 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AlarmHistoryEntity): Long

    /**
     * 히스토리 삭제.
     *
     * @param history 삭제할 히스토리
     */
    @Delete
    suspend fun deleteHistory(history: AlarmHistoryEntity)
}
