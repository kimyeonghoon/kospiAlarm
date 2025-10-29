package kim.yeonghoon.kospialarm.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * KospiResponse 단위 테스트.
 *
 * DTO-도메인 변환 로직을 테스트합니다.
 */
class KospiResponseTest {

    @Test
    fun `given KospiResponse with positive change, when toDomain is called, then should convert correctly`() {
        // 준비
        val response = KospiResponse(
            index = 2500.5,
            timestamp = 1000L,
            change = 25.5,
            changePercent = 1.03
        )

        // 실행
        val domain = response.toDomain()

        // 검증
        assertEquals(2500.5, domain.index, 0.001)
        assertEquals(1000L, domain.timestamp)
        assertEquals(25.5, domain.change, 0.001)
        assertEquals(1.03, domain.changePercent, 0.001)
    }

    @Test
    fun `given KospiResponse with negative change, when toDomain is called, then should convert correctly`() {
        // 준비
        val response = KospiResponse(
            index = 2475.0,
            timestamp = 2000L,
            change = -25.0,
            changePercent = -1.0
        )

        // 실행
        val domain = response.toDomain()

        // 검증
        assertEquals(2475.0, domain.index, 0.001)
        assertEquals(2000L, domain.timestamp)
        assertEquals(-25.0, domain.change, 0.001)
        assertEquals(-1.0, domain.changePercent, 0.001)
    }

    @Test
    fun `given KospiResponse with zero change, when toDomain is called, then should convert correctly`() {
        // 준비
        val response = KospiResponse(
            index = 2500.0,
            timestamp = 3000L,
            change = 0.0,
            changePercent = 0.0
        )

        // 실행
        val domain = response.toDomain()

        // 검증
        assertEquals(2500.0, domain.index, 0.001)
        assertEquals(3000L, domain.timestamp)
        assertEquals(0.0, domain.change, 0.001)
        assertEquals(0.0, domain.changePercent, 0.001)
    }

    @Test
    fun `given KospiResponse with large values, when toDomain is called, then should preserve precision`() {
        // 준비
        val response = KospiResponse(
            index = 3500.12345,
            timestamp = System.currentTimeMillis(),
            change = 150.67890,
            changePercent = 4.50123
        )

        // 실행
        val domain = response.toDomain()

        // 검증
        assertEquals(3500.12345, domain.index, 0.00001)
        assertEquals(150.67890, domain.change, 0.00001)
        assertEquals(4.50123, domain.changePercent, 0.00001)
    }

    @Test
    fun `given KospiResponse, when converted to domain, then all fields should match`() {
        // 준비
        val timestamp = System.currentTimeMillis()
        val response = KospiResponse(
            index = 2600.75,
            timestamp = timestamp,
            change = 100.75,
            changePercent = 4.03
        )

        // 실행
        val domain = response.toDomain()

        // 검증
        assertEquals(response.index, domain.index, 0.001)
        assertEquals(response.timestamp, domain.timestamp)
        assertEquals(response.change, domain.change, 0.001)
        assertEquals(response.changePercent, domain.changePercent, 0.001)
    }
}
