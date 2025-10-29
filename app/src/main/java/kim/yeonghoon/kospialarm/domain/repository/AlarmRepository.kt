package kim.yeonghoon.kospialarm.domain.repository

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmHistory
import kim.yeonghoon.kospialarm.domain.model.KospiData
import kim.yeonghoon.kospialarm.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * 알림 레포지토리 인터페이스.
 */
interface AlarmRepository {
    /**
     * 모든 알림을 Flow로 반환.
     *
     * @return 알림 목록 Flow
     */
    fun getAllAlarms(): Flow<List<Alarm>>

    /**
     * ID로 알림 조회.
     *
     * @param id 알림 ID
     * @return 알림 또는 null
     */
    suspend fun getAlarmById(id: Long): Alarm?

    /**
     * 알림 생성.
     *
     * @param alarm 생성할 알림
     * @return 생성된 알림 ID
     */
    suspend fun createAlarm(alarm: Alarm): Long

    /**
     * 알림 삭제.
     *
     * @param alarm 삭제할 알림
     */
    suspend fun deleteAlarm(alarm: Alarm)

    /**
     * 알림 활성화/비활성화 토글.
     *
     * @param alarmId 알림 ID
     * @param isEnabled 활성화 여부
     */
    suspend fun toggleAlarm(alarmId: Long, isEnabled: Boolean)

    /**
     * 현재 코스피 데이터 조회.
     *
     * @return Result로 래핑된 코스피 데이터
     */
    suspend fun getCurrentKospiData(): Result<KospiData>

    /**
     * 알림 히스토리 저장.
     *
     * @param history 저장할 히스토리
     */
    suspend fun saveAlarmHistory(history: AlarmHistory)

    /**
     * 모든 알림 히스토리 조회.
     *
     * @return 히스토리 목록 Flow
     */
    fun getAllHistory(): Flow<List<AlarmHistory>>
}
