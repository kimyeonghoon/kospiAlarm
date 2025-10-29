package kim.yeonghoon.kospialarm.util

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * SharedPreferences 헬퍼 클래스.
 */
object PreferencesHelper {

    private const val PREF_NAME = "kospi_alarm_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    /**
     * SharedPreferences 인스턴스 가져오기.
     *
     * @param context Context
     * @return SharedPreferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 생체인증 활성화 여부 저장.
     *
     * @param context Context
     * @param enabled 활성화 여부
     */
    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        Timber.d("setBiometricEnabled: enabled=$enabled")
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * 생체인증 활성화 여부 가져오기.
     *
     * @param context Context
     * @return 활성화 여부 (기본값: true)
     */
    fun isBiometricEnabled(context: Context): Boolean {
        val enabled = getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, true)
        Timber.d("isBiometricEnabled: enabled=$enabled")
        return enabled
    }
}
