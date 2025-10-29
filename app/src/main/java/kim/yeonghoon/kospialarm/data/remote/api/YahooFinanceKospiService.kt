package kim.yeonghoon.kospialarm.data.remote.api

import kim.yeonghoon.kospialarm.data.remote.dto.KospiResponse
import retrofit2.Response
import timber.log.Timber

/**
 * Yahoo Finance API를 사용하는 KOSPI 서비스.
 *
 * @property yahooApi Yahoo Finance API 서비스
 */
class YahooFinanceKospiService(
    private val yahooApi: YahooFinanceApiService
) : KospiApiService {

    /**
     * Yahoo Finance에서 현재 KOSPI 데이터 조회.
     *
     * @return 코스피 응답
     */
    override suspend fun getCurrentKospi(): Response<KospiResponse> {
        return try {
            Timber.d("YahooFinanceKospiService: Yahoo Finance에서 KOSPI 데이터 조회 시작")

            val response = yahooApi.getKospiChart()

            if (response.isSuccessful && response.body() != null) {
                val kospiResponse = response.body()!!.toKospiResponse()

                Timber.i(
                    "YahooFinanceKospiService: 데이터 조회 성공 - " +
                    "index=${kospiResponse.index}, " +
                    "change=${kospiResponse.change}, " +
                    "changePercent=${kospiResponse.changePercent}%"
                )

                Response.success(kospiResponse)
            } else {
                Timber.w("YahooFinanceKospiService: API 응답 실패 - code=${response.code()}")
                Response.error(response.code(), response.errorBody()!!)
            }
        } catch (e: Exception) {
            Timber.e(e, "YahooFinanceKospiService: 데이터 조회 중 오류 발생")
            Response.error(500, okhttp3.ResponseBody.create(null, e.message ?: "Unknown error"))
        }
    }
}
