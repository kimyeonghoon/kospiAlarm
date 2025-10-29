package kim.yeonghoon.kospialarm.data.remote.api

import kim.yeonghoon.kospialarm.data.remote.dto.KospiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

/**
 * 네이버 금융에서 KOSPI 데이터를 스크래핑하는 서비스.
 *
 * 네이버 금융 KOSPI 페이지를 HTML 파싱하여 실시간 지수를 가져옵니다.
 */
class NaverFinanceKospiService : KospiApiService {

    companion object {
        private const val NAVER_FINANCE_KOSPI_URL = "https://finance.naver.com/sise/sise_index.naver?code=KOSPI"
        private const val TIMEOUT_MS = 10000
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36"
    }

    /**
     * 네이버 금융에서 현재 KOSPI 데이터 조회.
     *
     * @return 코스피 응답
     */
    override suspend fun getCurrentKospi(): Response<KospiResponse> = withContext(Dispatchers.IO) {
        try {
            Timber.d("NaverFinanceKospiService: 네이버 금융에서 KOSPI 데이터 가져오기 시작")

            // 네이버 금융 페이지 HTML 가져오기
            val doc = Jsoup.connect(NAVER_FINANCE_KOSPI_URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            Timber.d("NaverFinanceKospiService: HTML 다운로드 완료")

            // 현재 지수 파싱 (여러 선택자 시도)
            val nowValText = doc.select("#now_value").text().ifEmpty {
                doc.select("div.rate_info div.today span.blind").text().ifEmpty {
                    doc.select("em#_nowVal").text()
                }
            }

            Timber.d("NaverFinanceKospiService: nowVal text = '$nowValText'")

            val nowVal = nowValText
                .replace(",", "")
                .replace(" ", "")
                .toDoubleOrNull() ?: throw IOException("현재가 파싱 실패: '$nowValText'")

            // 전일대비와 등락률 파싱
            val changeElements = doc.select("div.rate_info div.today span.change_value_and_rate")
            val changeText = if (changeElements.isEmpty()) {
                doc.select("#change_value_and_rate").text()
            } else {
                changeElements.text()
            }

            Timber.d("NaverFinanceKospiService: change text = '$changeText'")

            // 간단한 파싱: "상승 20.00 +0.82%" 또는 "하락 20.00 -0.82%" 형태
            val isUp = changeText.contains("상승") || changeText.contains("+")
            val isDown = changeText.contains("하락") || changeText.contains("-")

            // 숫자 추출
            val numbers = Regex("\\d+\\.\\d+").findAll(changeText).map { it.value.toDouble() }.toList()

            val actualChange = if (numbers.isNotEmpty()) {
                val change = numbers[0]
                when {
                    isDown -> -change
                    else -> change
                }
            } else {
                0.0
            }

            val actualChangeRate = if (numbers.size >= 2) {
                val rate = numbers[1]
                when {
                    isDown -> -rate
                    else -> rate
                }
            } else {
                0.0
            }

            val kospiResponse = KospiResponse(
                index = nowVal,
                timestamp = System.currentTimeMillis(),
                change = actualChange,
                changePercent = actualChangeRate
            )

            Timber.i("NaverFinanceKospiService: 데이터 파싱 성공 - index=$nowVal, change=$actualChange, rate=$actualChangeRate%")

            Response.success(kospiResponse)
        } catch (e: IOException) {
            Timber.e(e, "NaverFinanceKospiService: 네트워크 오류")
            Response.error(500, okhttp3.ResponseBody.create(null, e.message ?: "Network error"))
        } catch (e: Exception) {
            Timber.e(e, "NaverFinanceKospiService: 파싱 오류")
            Response.error(500, okhttp3.ResponseBody.create(null, e.message ?: "Parsing error"))
        }
    }
}
