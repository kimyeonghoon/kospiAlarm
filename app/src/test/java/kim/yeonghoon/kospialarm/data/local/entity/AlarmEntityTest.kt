package kim.yeonghoon.kospialarm.data.local.entity

import kim.yeonghoon.kospialarm.domain.model.Alarm
import kim.yeonghoon.kospialarm.domain.model.AlarmType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AlarmEntity 단위 테스트.
 *
 * 도메인-엔티티 변환 로직을 테스트합니다.
 */
class AlarmEntityTest {

    @Test
    fun `given AlarmEntity, when toDomain is called, then should convert to Alarm correctly`() {
        // 준비
        val entity = AlarmEntity(
            id = 1,
            baseValue = 2500.0,
            percentage = 5,
            type = "RISE",
            isEnabled = true,
            createdAt = 1000L
        )

        // 실행
        val domain = entity.toDomain()

        // 검증
        assertEquals(1L, domain.id)
        assertEquals(2500.0, domain.baseValue, 0.001)
        assertEquals(5, domain.percentage)
        assertEquals(AlarmType.RISE, domain.type)
        assertEquals(true, domain.isEnabled)
        assertEquals(1000L, domain.createdAt)
    }

    @Test
    fun `given AlarmEntity with FALL type, when toDomain is called, then should convert type correctly`() {
        // 준비
        val entity = AlarmEntity(
            id = 2,
            baseValue = 2600.0,
            percentage = 10,
            type = "FALL",
            isEnabled = false,
            createdAt = 2000L
        )

        // 실행
        val domain = entity.toDomain()

        // 검증
        assertEquals(AlarmType.FALL, domain.type)
        assertEquals(false, domain.isEnabled)
    }

    @Test
    fun `given Alarm domain model, when fromDomain is called, then should convert to AlarmEntity correctly`() {
        // 준비
        val alarm = Alarm(
            id = 3,
            baseValue = 2700.0,
            percentage = 15,
            type = AlarmType.RISE,
            isEnabled = true,
            createdAt = 3000L
        )

        // 실행
        val entity = AlarmEntity.fromDomain(alarm)

        // 검증
        assertEquals(3L, entity.id)
        assertEquals(2700.0, entity.baseValue, 0.001)
        assertEquals(15, entity.percentage)
        assertEquals("RISE", entity.type)
        assertEquals(true, entity.isEnabled)
        assertEquals(3000L, entity.createdAt)
    }

    @Test
    fun `given Alarm with FALL type, when fromDomain is called, then should convert type to string correctly`() {
        // 준비
        val alarm = Alarm(
            id = 4,
            baseValue = 2400.0,
            percentage = 20,
            type = AlarmType.FALL,
            isEnabled = false,
            createdAt = 4000L
        )

        // 실행
        val entity = AlarmEntity.fromDomain(alarm)

        // 검증
        assertEquals("FALL", entity.type)
        assertEquals(false, entity.isEnabled)
    }

    @Test
    fun `given domain to entity to domain conversion, when converted, then should preserve all data`() {
        // 준비
        val originalAlarm = Alarm(
            id = 5,
            baseValue = 2500.5,
            percentage = 5,
            type = AlarmType.RISE,
            isEnabled = true,
            createdAt = 5000L
        )

        // 실행
        val entity = AlarmEntity.fromDomain(originalAlarm)
        val convertedAlarm = entity.toDomain()

        // 검증
        assertEquals(originalAlarm.id, convertedAlarm.id)
        assertEquals(originalAlarm.baseValue, convertedAlarm.baseValue, 0.001)
        assertEquals(originalAlarm.percentage, convertedAlarm.percentage)
        assertEquals(originalAlarm.type, convertedAlarm.type)
        assertEquals(originalAlarm.isEnabled, convertedAlarm.isEnabled)
        assertEquals(originalAlarm.createdAt, convertedAlarm.createdAt)
    }

    @Test
    fun `given entity to domain to entity conversion, when converted, then should preserve all data`() {
        // 준비
        val originalEntity = AlarmEntity(
            id = 6,
            baseValue = 2600.5,
            percentage = 10,
            type = "FALL",
            isEnabled = false,
            createdAt = 6000L
        )

        // 실행
        val domain = originalEntity.toDomain()
        val convertedEntity = AlarmEntity.fromDomain(domain)

        // 검증
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.baseValue, convertedEntity.baseValue, 0.001)
        assertEquals(originalEntity.percentage, convertedEntity.percentage)
        assertEquals(originalEntity.type, convertedEntity.type)
        assertEquals(originalEntity.isEnabled, convertedEntity.isEnabled)
        assertEquals(originalEntity.createdAt, convertedEntity.createdAt)
    }
}
