package com.ioniere.kospialarm.util

/**
 * API 응답 및 비즈니스 로직 결과를 나타내는 sealed class.
 *
 * @param T 성공 시 반환되는 데이터 타입
 */
sealed class Result<out T> {
    /**
     * 성공 결과.
     *
     * @property data 성공 시 반환되는 데이터
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * 에러 결과.
     *
     * @property exception 발생한 예외
     */
    data class Error(val exception: Throwable) : Result<Nothing>()

    /**
     * 로딩 상태.
     */
    data object Loading : Result<Nothing>()
}
