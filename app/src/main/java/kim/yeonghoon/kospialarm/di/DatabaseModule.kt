package kim.yeonghoon.kospialarm.di

import android.content.Context
import androidx.room.Room
import kim.yeonghoon.kospialarm.data.local.AppDatabase
import kim.yeonghoon.kospialarm.data.local.dao.AlarmDao
import kim.yeonghoon.kospialarm.data.local.dao.AlarmHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 데이터베이스 관련 의존성 주입 모듈.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * AppDatabase 인스턴스 제공.
     *
     * @param context Application Context
     * @return AppDatabase 인스턴스
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kospi_alarm_db"
        ).build()
    }

    /**
     * AlarmDao 제공.
     *
     * @param database AppDatabase 인스턴스
     * @return AlarmDao
     */
    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }

    /**
     * AlarmHistoryDao 제공.
     *
     * @param database AppDatabase 인스턴스
     * @return AlarmHistoryDao
     */
    @Provides
    @Singleton
    fun provideAlarmHistoryDao(database: AppDatabase): AlarmHistoryDao {
        return database.alarmHistoryDao()
    }
}
