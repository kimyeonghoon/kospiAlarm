package com.ioniere.kospialarm.domain.usecase

import com.ioniere.kospialarm.domain.model.KospiData
import com.ioniere.kospialarm.domain.repository.AlarmRepository
import com.ioniere.kospialarm.util.Result
import javax.inject.Inject

/**
 * 현재 코스피 데이터를 조회하는 유스케이스.
 *
 * @property repository 알림 레포지토리
 */
class GetCurrentKospiUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    /**
     * 현재 코스피 데이터를 조회.
     *
     * @return Result로 래핑된 코스피 데이터
     */
    suspend operator fun invoke(): Result<KospiData> {
        return repository.getCurrentKospiData()
    }
}
