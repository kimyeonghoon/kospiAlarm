package com.ioniere.kospialarm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * KOSPI Alarm Application 클래스.
 * Hilt 및 Timber 초기화.
 */
@HiltAndroidApp
class KospiAlarmApplication : Application() {

    /**
     * Application 생성 시 초기화.
     */
    override fun onCreate() {
        super.onCreate()

        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Application created - Debug mode")
        }

        Timber.i("KospiAlarmApplication initialized")
    }
}
