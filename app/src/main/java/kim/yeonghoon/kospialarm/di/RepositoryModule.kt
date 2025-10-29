package kim.yeonghoon.kospialarm.di

import kim.yeonghoon.kospialarm.data.repository.AlarmRepositoryImpl
import kim.yeonghoon.kospialarm.domain.repository.AlarmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 레포지토리 관련 의존성 주입 모듈.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * AlarmRepository 구현체 바인딩.
     *
     * @param impl AlarmRepositoryImpl 구현체
     * @return AlarmRepository 인터페이스
     */
    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmRepositoryImpl
    ): AlarmRepository
}
