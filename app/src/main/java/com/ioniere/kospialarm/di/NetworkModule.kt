package com.ioniere.kospialarm.di

import com.ioniere.kospialarm.data.remote.api.KospiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
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
     * @param retrofit Retrofit 인스턴스
     * @return KospiApiService
     */
    @Provides
    @Singleton
    fun provideKospiApiService(retrofit: Retrofit): KospiApiService {
        return retrofit.create(KospiApiService::class.java)
    }
}
