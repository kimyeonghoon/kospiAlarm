package kim.yeonghoon.kospialarm.di

import kim.yeonghoon.kospialarm.BuildConfig
import kim.yeonghoon.kospialarm.data.remote.api.KospiApiService
import kim.yeonghoon.kospialarm.data.remote.api.MockKospiApiService
import kim.yeonghoon.kospialarm.data.remote.api.NaverFinanceKospiService
import kim.yeonghoon.kospialarm.data.remote.api.YahooFinanceApiService
import kim.yeonghoon.kospialarm.data.remote.api.YahooFinanceKospiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성 주입 모듈.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val YAHOO_FINANCE_BASE_URL = "https://query1.finance.yahoo.com/"
    private const val TIMEOUT_SECONDS = 15L
    private const val USE_MOCK_API = false // true = Mock 데이터 (테스트용)
    private const val USE_YAHOO_FINANCE = true // true = Yahoo Finance 실제 KOSPI 데이터
    private const val USE_NAVER_FINANCE = false // 네이버 금융 사용 여부 (현재 동작 안 함)

    /**
     * OkHttpClient 제공.
     *
     * @return OkHttpClient 인스턴스
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                    .build()
                chain.proceed(request)
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }

    /**
     * Yahoo Finance Retrofit 인스턴스 제공.
     *
     * @param okHttpClient OkHttpClient 인스턴스
     * @return Retrofit 인스턴스
     */
    @Provides
    @Singleton
    fun provideYahooFinanceRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(YAHOO_FINANCE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * YahooFinanceApiService 제공.
     *
     * @param retrofit Retrofit 인스턴스
     * @return YahooFinanceApiService
     */
    @Provides
    @Singleton
    fun provideYahooFinanceApiService(retrofit: Retrofit): YahooFinanceApiService {
        return retrofit.create(YahooFinanceApiService::class.java)
    }

    /**
     * KospiApiService 제공.
     *
     * 설정에 따라 제공합니다:
     * - USE_MOCK_API = true: Mock 데이터 (테스트용)
     * - USE_YAHOO_FINANCE = true: Yahoo Finance 실제 KOSPI 데이터
     * - USE_NAVER_FINANCE = true: 네이버 금융 실제 데이터 (스크래핑)
     *
     * @param yahooApi Yahoo Finance API 서비스
     * @return KospiApiService
     */
    @Provides
    @Singleton
    fun provideKospiApiService(yahooApi: YahooFinanceApiService): KospiApiService {
        return when {
            USE_MOCK_API -> {
                MockKospiApiService()
            }
            USE_YAHOO_FINANCE -> {
                YahooFinanceKospiService(yahooApi)
            }
            USE_NAVER_FINANCE -> {
                NaverFinanceKospiService()
            }
            else -> {
                throw IllegalStateException("No API source configured")
            }
        }
    }
}
