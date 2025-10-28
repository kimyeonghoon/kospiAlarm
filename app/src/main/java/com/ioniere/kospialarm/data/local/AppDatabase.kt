package com.ioniere.kospialarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ioniere.kospialarm.data.local.dao.AlarmDao
import com.ioniere.kospialarm.data.local.dao.AlarmHistoryDao
import com.ioniere.kospialarm.data.local.entity.AlarmEntity
import com.ioniere.kospialarm.data.local.entity.AlarmHistoryEntity

/**
 * Room 데이터베이스.
 */
@Database(
    entities = [AlarmEntity::class, AlarmHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 알림 DAO 반환.
     *
     * @return AlarmDao
     */
    abstract fun alarmDao(): AlarmDao

    /**
     * 알림 히스토리 DAO 반환.
     *
     * @return AlarmHistoryDao
     */
    abstract fun alarmHistoryDao(): AlarmHistoryDao
}
