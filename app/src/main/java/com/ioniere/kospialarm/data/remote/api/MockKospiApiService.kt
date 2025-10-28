package com.ioniere.kospialarm.data.remote.api

import com.ioniere.kospialarm.data.remote.dto.KospiResponse
import retrofit2.Response
import timber.log.Timber
import kotlin.random.Random

/**
 * Mock 코스피 API 서비스 (테스트용).
 *
 * 실제 API가 준비되기 전까지 사용하는 Mock 구현체입니다.
 */
class MockKospiApiService : KospiApiService {

    private var baseIndex = 2500.0

    /**
     * Mock 코스피 데이터 반환.
     *
     * @return 코스피 응답
     */
    override suspend fun getCurrentKospi(): Response<KospiResponse> {
        Timber.d("MockKospiApiService: getCurrentKospi 호출")

        // 랜덤으로 -2% ~ +2% 변동
        val changePercent = Random.nextDouble(-2.0, 2.0)
        val change = baseIndex * (changePercent / 100)
        val currentIndex = baseIndex + change

        // 다음 호출을 위해 현재 값을 베이스로 설정
        baseIndex = currentIndex

        val response = KospiResponse(
            index = currentIndex,
            timestamp = System.currentTimeMillis(),
            change = change,
            changePercent = changePercent
        )

        Timber.i("MockKospiApiService: Mock 데이터 생성 - index=$currentIndex, change=$changePercent%")

        return Response.success(response)
    }
}
