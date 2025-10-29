package kim.yeonghoon.kospialarm.di

import kim.yeonghoon.kospialarm.BuildConfig
import kim.yeonghoon.kospialarm.data.remote.api.KospiApiService
import kim.yeonghoon.kospialarm.data.remote.api.MockKospiApiService
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

    private const val BASE_URL = "https://api.example.com/" // TODO: 실제 API URL로 변경
    private const val TIMEOUT_SECONDS = 15L
    private const val USE_MOCK_API = true // Mock API 사용 여부

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
     * Retrofit 인스턴스 제공.
     *
     * @param okHttpClient OkHttpClient 인스턴스
     * @return Retrofit 인스턴스
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * KospiApiService 제공.
     *
     * Mock API 또는 실제 API를 USE_MOCK_API 설정에 따라 제공합니다.
     *
     * @param retrofit Retrofit 인스턴스
     * @return KospiApiService
     */
    @Provides
    @Singleton
    fun provideKospiApiService(retrofit: Retrofit): KospiApiService {
        return if (USE_MOCK_API) {
            MockKospiApiService()
        } else {
            retrofit.create(KospiApiService::class.java)
        }
    }
}
