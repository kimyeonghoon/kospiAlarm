package com.ioniere.kospialarm.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ioniere.kospialarm.domain.model.KospiData

/**
 * 코스피 API 응답 DTO.
 *
 * @property index 현재 코스피 지수
 * @property timestamp 데이터 시간 (Unix timestamp)
 * @property change 전일 대비 변화량
 * @property changePercent 전일 대비 변화율(%)
 */
data class KospiResponse(
    @SerializedName("index")
    val index: Double,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("change")
    val change: Double,
    @SerializedName("changePercent")
    val changePercent: Double
) {
    /**
     * DTO를 도메인 모델로 변환.
     *
     * @return KospiData 도메인 모델
     */
    fun toDomain(): KospiData {
        return KospiData(
            index = index,
            timestamp = timestamp,
            change = change,
            changePercent = changePercent
        )
    }
}
