package kim.yeonghoon.kospialarm.data.remote.api

import kim.yeonghoon.kospialarm.data.remote.dto.YahooFinanceResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Yahoo Finance API 서비스.
 *
 * KOSPI 지수 (^KS11)를 조회합니다.
 */
interface YahooFinanceApiService {
    /**
     * KOSPI 차트 데이터 조회.
     *
     * @return Yahoo Finance 응답
     */
    @GET("v8/finance/chart/%5EKS11?interval=1d")
    suspend fun getKospiChart(): Response<YahooFinanceResponse>
}
