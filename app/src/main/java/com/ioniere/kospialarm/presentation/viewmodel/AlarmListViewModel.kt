package com.ioniere.kospialarm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ioniere.kospialarm.domain.model.Alarm
import com.ioniere.kospialarm.domain.model.KospiData
import com.ioniere.kospialarm.domain.usecase.CreateAlarmUseCase
import com.ioniere.kospialarm.domain.usecase.DeleteAlarmUseCase
import com.ioniere.kospialarm.domain.usecase.GetAlarmsUseCase
import com.ioniere.kospialarm.domain.usecase.GetCurrentKospiUseCase
import com.ioniere.kospialarm.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 알림 목록 ViewModel.
 *
 * @property getAlarmsUseCase GetAlarmsUseCase
 * @property createAlarmUseCase CreateAlarmUseCase
 * @property deleteAlarmUseCase DeleteAlarmUseCase
 * @property getCurrentKospiUseCase GetCurrentKospiUseCase
 */
@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val getCurrentKospiUseCase: GetCurrentKospiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlarmListUiState>(AlarmListUiState.Loading)
    val uiState: StateFlow<AlarmListUiState> = _uiState.asStateFlow()

    private val _kospiData = MutableStateFlow<KospiData?>(null)
    val kospiData: StateFlow<KospiData?> = _kospiData.asStateFlow()

    init {
        Timber.d("AlarmListViewModel initialized")
        loadAlarms()
        loadKospiData()
    }

    /**
     * 알림 목록 로드.
     */
    private fun loadAlarms() {
        Timber.d("loadAlarms: 알림 목록 로드 시작")
        viewModelScope.launch {
            getAlarmsUseCase()
                .catch { e ->
                    Timber.e(e, "loadAlarms: 알림 목록 로드 실패")
                    _uiState.value = AlarmListUiState.Error(e.message ?: "알 수 없는 오류")
                }
                .collect { alarms ->
                    Timber.i("loadAlarms: ${alarms.size}개의 알림 로드 완료")
                    _uiState.value = AlarmListUiState.Success(alarms)
                }
        }
    }

    /**
     * 코스피 데이터 로드.
     */
    fun loadKospiData() {
        Timber.d("loadKospiData: 코스피 데이터 로드 시작")
        viewModelScope.launch {
            when (val result = getCurrentKospiUseCase()) {
                is Result.Success -> {
                    Timber.i("loadKospiData: 코스피 데이터 로드 성공")
                    _kospiData.value = result.data
                }
                is Result.Error -> {
                    Timber.w("loadKospiData: 코스피 데이터 로드 실패", result.exception)
                    _kospiData.value = null
                }
                is Result.Loading -> {
                    Timber.d("loadKospiData: 로딩 중")
                }
            }
        }
    }

    /**
     * 알림 생성.
     *
     * @param alarm 생성할 알림
     */
    fun createAlarm(alarm: Alarm) {
        Timber.d("createAlarm: 알림 생성 시작")
        viewModelScope.launch {
            try {
                val id = createAlarmUseCase(alarm)
                Timber.i("createAlarm: 알림 생성 완료, id=$id")
            } catch (e: Exception) {
                Timber.e(e, "createAlarm: 알림 생성 실패")
            }
        }
    }

    /**
     * 알림 삭제.
     *
     * @param alarm 삭제할 알림
     */
    fun deleteAlarm(alarm: Alarm) {
        Timber.d("deleteAlarm: 알림 삭제 시작, id=${alarm.id}")
        viewModelScope.launch {
            try {
                deleteAlarmUseCase(alarm)
                Timber.i("deleteAlarm: 알림 삭제 완료")
            } catch (e: Exception) {
                Timber.e(e, "deleteAlarm: 알림 삭제 실패")
            }
        }
    }
}

/**
 * 알림 목록 UI 상태.
 */
sealed class AlarmListUiState {
    /**
     * 로딩 상태.
     */
    data object Loading : AlarmListUiState()

    /**
     * 성공 상태.
     *
     * @property alarms 알림 목록
     */
    data class Success(val alarms: List<Alarm>) : AlarmListUiState()

    /**
     * 에러 상태.
     *
     * @property message 에러 메시지
     */
    data class Error(val message: String) : AlarmListUiState()
}
