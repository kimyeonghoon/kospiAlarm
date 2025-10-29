package kim.yeonghoon.kospialarm.data.repository

import kim.yeonghoon.kospialarm.data.local.dao.AlarmDao
import kim.yeonghoon.kospialarm.data.local.dao.AlarmHistoryDao
import kim.yeonghoon.kospialarm.data.local.entity.AlarmEntity
import kim.yeonghoon.kospialarm.data.local.entity.AlarmHistoryEntity
import kim.yeonghoon.kospialarm.data.remote.api.KospiApiService
import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmHistory
import kim.yeonghoon.kospialarm.domain.model.KospiData
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import kim.yeonghoon.kospialarm.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 레포지토리 구현체.
 *
 * @property alarmDao 알림 DAO
 * @property historyDao 히스토리 DAO
 * @property apiService 코스피 API 서비스
 */
@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val historyDao: AlarmHistoryDao,
    private val apiService: KospiApiService
) : AlarmRepository {

    /**
     * 모든 알림을 Flow로 반환.
     *
     * @return 알림 목록 Flow
     */
    override fun getAllAlarms(): Flow<List<Alarm>> {
        Timber.d("getAllAlarms: 알림 목록 조회 시작")
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }.also {
                Timber.i("getAllAlarms: ${it.size}개의 알림 조회 완료")
            }
        }
    }

    /**
     * ID로 알림 조회.
     *
     * @param id 알림 ID
     * @return 알림 또는 null
     */
    override suspend fun getAlarmById(id: Long): Alarm? {
        Timber.d("getAlarmById: ID=$id 알림 조회 시작")
        return alarmDao.getAlarmById(id)?.toDomain().also {
            Timber.i("getAlarmById: ID=$id 알림 조회 완료, 결과=${it != null}")
        }
    }

    /**
     * 알림 생성.
     *
     * @param alarm 생성할 알림
     * @return 생성된 알림 ID
     */
    override suspend fun createAlarm(alarm: Alarm): Long {
        Timber.d("createAlarm: 알림 생성 시작 - ${alarm.percentage}% ${alarm.type}")
        return alarmDao.insertAlarm(AlarmEntity.fromDomain(alarm)).also { id ->
            Timber.i("createAlarm: 알림 생성 완료, ID=$id")
        }
    }

    /**
     * 알림 삭제.
     *
     * @param alarm 삭제할 알림
     */
    override suspend fun deleteAlarm(alarm: Alarm) {
        Timber.d("deleteAlarm: 알림 삭제 시작, ID=${alarm.id}")
        alarmDao.deleteAlarm(AlarmEntity.fromDomain(alarm))
        Timber.i("deleteAlarm: 알림 삭제 완료, ID=${alarm.id}")
    }

    /**
     * 알림 활성화/비활성화 토글.
     *
     * @param alarmId 알림 ID
     * @param isEnabled 활성화 여부
     */
    override suspend fun toggleAlarm(alarmId: Long, isEnabled: Boolean) {
        Timber.d("toggleAlarm: 알림 상태 변경, ID=$alarmId, isEnabled=$isEnabled")
        alarmDao.updateAlarmEnabled(alarmId, isEnabled)
        Timber.i("toggleAlarm: 알림 상태 변경 완료")
    }

    /**
     * 현재 코스피 데이터 조회.
     *
     * @return Result로 래핑된 코스피 데이터
     */
    override suspend fun getCurrentKospiData(): Result<KospiData> {
        Timber.d("getCurrentKospiData: 코스피 데이터 조회 시작")
        return try {
            val response = apiService.getCurrentKospi()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.toDomain()
                Timber.i("getCurrentKospiData: 코스피 데이터 조회 성공, index=${data.index}")
                Result.Success(data)
            } else {
                val errorMsg = "API 응답 실패: code=${response.code()}"
                Timber.w("getCurrentKospiData: $errorMsg")
                Result.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Timber.e(e, "getCurrentKospiData: 코스피 데이터 조회 중 예외 발생")
            Result.Error(e)
        }
    }

    /**
     * 알림 히스토리 저장.
     *
     * @param history 저장할 히스토리
     */
    override suspend fun saveAlarmHistory(history: AlarmHistory) {
        Timber.d("saveAlarmHistory: 히스토리 저장 시작, alarmId=${history.alarmId}")
        historyDao.insertHistory(AlarmHistoryEntity.fromDomain(history))
        Timber.i("saveAlarmHistory: 히스토리 저장 완료")
    }

    /**
     * 모든 알림 히스토리 조회.
     *
     * @return 히스토리 목록 Flow
     */
    override fun getAllHistory(): Flow<List<AlarmHistory>> {
        Timber.d("getAllHistory: 히스토리 목록 조회 시작")
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }.also {
                Timber.i("getAllHistory: ${it.size}개의 히스토리 조회 완료")
            }
        }
    }
}
