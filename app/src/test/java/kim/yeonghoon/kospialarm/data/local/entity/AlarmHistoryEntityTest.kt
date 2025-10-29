package kim.yeonghoon.kospialarm.data.local.entity

import kim.yeonghoon.kospialarm.domain.model.AlarmHistory
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AlarmHistoryEntity 단위 테스트.
 *
 * 도메인-엔티티 변환 로직을 테스트합니다.
 */
class AlarmHistoryEntityTest {

    @Test
    fun `given AlarmHistoryEntity, when toDomain is called, then should convert to AlarmHistory correctly`() {
        // 준비
        val entity = AlarmHistoryEntity(
            id = 1,
            alarmId = 10,
            triggeredValue = 2625.0,
            triggeredAt = 1000L,
            baseValue = 2500.0,
            percentage = 5,
            type = "RISE"
        )

        // 실행
        val domain = entity.toDomain()

        // 검증
        assertEquals(1L, domain.id)
        assertEquals(10L, domain.alarmId)
        assertEquals(2625.0, domain.triggeredValue, 0.001)
        assertEquals(1000L, domain.triggeredAt)
        assertEquals(2500.0, domain.baseValue, 0.001)
        assertEquals(5, domain.percentage)
        assertEquals(AlarmType.RISE, domain.type)
    }

    @Test
    fun `given AlarmHistoryEntity with FALL type, when toDomain is called, then should convert type correctly`() {
        // 준비
        val entity = AlarmHistoryEntity(
            id = 2,
            alarmId = 20,
            triggeredValue = 2375.0,
            triggeredAt = 2000L,
            baseValue = 2500.0,
            percentage = 5,
            type = "FALL"
        )

        // 실행
        val domain = entity.toDomain()

        // 검증
        assertEquals(AlarmType.FALL, domain.type)
        assertEquals(2375.0, domain.triggeredValue, 0.001)
    }

    @Test
    fun `given AlarmHistory domain model, when fromDomain is called, then should convert to AlarmHistoryEntity correctly`() {
        // 준비
        val history = AlarmHistory(
            id = 3,
            alarmId = 30,
            triggeredValue = 2750.0,
            triggeredAt = 3000L,
            baseValue = 2500.0,
            percentage = 10,
            type = AlarmType.RISE
        )

        // 실행
        val entity = AlarmHistoryEntity.fromDomain(history)

        // 검증
        assertEquals(3L, entity.id)
        assertEquals(30L, entity.alarmId)
        assertEquals(2750.0, entity.triggeredValue, 0.001)
        assertEquals(3000L, entity.triggeredAt)
        assertEquals(2500.0, entity.baseValue, 0.001)
        assertEquals(10, entity.percentage)
        assertEquals("RISE", entity.type)
    }

    @Test
    fun `given AlarmHistory with FALL type, when fromDomain is called, then should convert type to string correctly`() {
        // 준비
        val history = AlarmHistory(
            id = 4,
            alarmId = 40,
            triggeredValue = 2250.0,
            triggeredAt = 4000L,
            baseValue = 2500.0,
            percentage = 10,
            type = AlarmType.FALL
        )

        // 실행
        val entity = AlarmHistoryEntity.fromDomain(history)

        // 검증
        assertEquals("FALL", entity.type)
        assertEquals(2250.0, entity.triggeredValue, 0.001)
    }

    @Test
    fun `given domain to entity to domain conversion, when converted, then should preserve all data`() {
        // 준비
        val originalHistory = AlarmHistory(
            id = 5,
            alarmId = 50,
            triggeredValue = 2625.5,
            triggeredAt = 5000L,
            baseValue = 2500.5,
            percentage = 5,
            type = AlarmType.RISE
        )

        // 실행
        val entity = AlarmHistoryEntity.fromDomain(originalHistory)
        val convertedHistory = entity.toDomain()

        // 검증
        assertEquals(originalHistory.id, convertedHistory.id)
        assertEquals(originalHistory.alarmId, convertedHistory.alarmId)
        assertEquals(originalHistory.triggeredValue, convertedHistory.triggeredValue, 0.001)
        assertEquals(originalHistory.triggeredAt, convertedHistory.triggeredAt)
        assertEquals(originalHistory.baseValue, convertedHistory.baseValue, 0.001)
        assertEquals(originalHistory.percentage, convertedHistory.percentage)
        assertEquals(originalHistory.type, convertedHistory.type)
    }

    @Test
    fun `given entity to domain to entity conversion, when converted, then should preserve all data`() {
        // 준비
        val originalEntity = AlarmHistoryEntity(
            id = 6,
            alarmId = 60,
            triggeredValue = 2375.5,
            triggeredAt = 6000L,
            baseValue = 2500.5,
            percentage = 5,
            type = "FALL"
        )

        // 실행
        val domain = originalEntity.toDomain()
        val convertedEntity = AlarmHistoryEntity.fromDomain(domain)

        // 검증
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.alarmId, convertedEntity.alarmId)
        assertEquals(originalEntity.triggeredValue, convertedEntity.triggeredValue, 0.001)
        assertEquals(originalEntity.triggeredAt, convertedEntity.triggeredAt)
        assertEquals(originalEntity.baseValue, convertedEntity.baseValue, 0.001)
        assertEquals(originalEntity.percentage, convertedEntity.percentage)
        assertEquals(originalEntity.type, convertedEntity.type)
    }
}
