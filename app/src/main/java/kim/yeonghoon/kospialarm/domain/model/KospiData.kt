package kim.yeonghoon.kospialarm.domain.model

/**
 * 코스피 데이터 도메인 모델.
 *
 * @property index 현재 코스피 지수
 * @property timestamp 데이터 시간 (Unix timestamp)
 * @property change 전일 대비 변화량
 * @property changePercent 전일 대비 변화율(%)
 */
data class KospiData(
    val index: Double,
    val timestamp: Long,
    val change: Double,
    val changePercent: Double
)
