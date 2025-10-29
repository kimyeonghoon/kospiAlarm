package kim.yeonghoon.kospialarm.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Yahoo Finance API 응답 DTO.
 */
data class YahooFinanceResponse(
    @SerializedName("chart")
    val chart: Chart
) {
    data class Chart(
        @SerializedName("result")
        val result: List<Result>?
    )

    data class Result(
        @SerializedName("meta")
        val meta: Meta
    )

    data class Meta(
        @SerializedName("regularMarketPrice")
        val regularMarketPrice: Double,
        @SerializedName("chartPreviousClose")
        val chartPreviousClose: Double,
        @SerializedName("currency")
        val currency: String,
        @SerializedName("symbol")
        val symbol: String
    )

    /**
     * KospiResponse로 변환.
     *
     * @return KospiResponse
     */
    fun toKospiResponse(): KospiResponse {
        val meta = chart.result?.firstOrNull()?.meta
            ?: throw IllegalStateException("Yahoo Finance 응답에 데이터가 없습니다")

        val currentPrice = meta.regularMarketPrice
        val previousClose = meta.chartPreviousClose
        val change = currentPrice - previousClose
        val changePercent = (change / previousClose) * 100

        return KospiResponse(
            index = currentPrice,
            timestamp = System.currentTimeMillis(),
            change = change,
            changePercent = changePercent
        )
    }
}
