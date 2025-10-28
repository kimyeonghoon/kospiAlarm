package com.ioniere.kospialarm.data.remote.api

import com.ioniere.kospialarm.data.remote.dto.KospiResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * 코스피 API 서비스.
 *
 * TODO: 실제 API 엔드포인트로 변경 필요
 */
interface KospiApiService {
    /**
     * 현재 코스피 데이터 조회.
     *
     * @return 코스피 응답
     */
    @GET("kospi/current")
    suspend fun getCurrentKospi(): Response<KospiResponse>
}
