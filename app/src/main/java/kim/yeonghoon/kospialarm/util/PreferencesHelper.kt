package kim.yeonghoon.kospialarm.util

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * SharedPreferences 헬퍼 클래스.
 *
 * 앱의 간단한 설정 값들을 저장하고 불러오는 유틸리티 클래스입니다.
 * 현재는 생체인증 활성화 여부만 저장하지만, 향후 다른 설정들을 추가할 수 있습니다.
 *
 * 저장 위치: /data/data/kim.yeonghoon.kospialarm/shared_prefs/kospi_alarm_prefs.xml
 * 보안 수준: MODE_PRIVATE (앱 내부에서만 접근 가능)
 *
 * 향후 추가 가능한 설정:
 * - 알림 소리 설정
 * - 진동 패턴 설정
 * - 다크 모드 설정
 */
object PreferencesHelper {

    /** SharedPreferences 파일 이름 */
    private const val PREF_NAME = "kospi_alarm_prefs"

    /** 생체인증 활성화 여부 키 */
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    /**
     * SharedPreferences 인스턴스 가져오기.
     *
     * MODE_PRIVATE를 사용하여 앱 내부에서만 접근 가능한
     * SharedPreferences 인스턴스를 반환합니다.
     *
     * @param context Context 인스턴스
     * @return SharedPreferences 인스턴스
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 생체인증 활성화 여부 저장.
     *
     * 사용자가 설정 화면에서 생체인증 기능을 켜거나 끌 때 호출됩니다.
     * 변경사항은 즉시 디스크에 저장됩니다 (apply 사용).
     *
     * 참고:
     * - commit()은 동기적으로 저장하며 UI 스레드를 블록할 수 있음
     * - apply()는 비동기적으로 저장하며 UI 스레드를 블록하지 않음
     * - 여기서는 apply()를 사용하여 성능 최적화
     *
     * @param context Context 인스턴스
     * @param enabled 생체인증 활성화 여부
     *                - true: 생체인증 활성화 (앱 실행 시 지문인증 필요)
     *                - false: 생체인증 비활성화 (인증 없이 바로 진입)
     */
    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        Timber.d("setBiometricEnabled: enabled=$enabled")
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * 생체인증 활성화 여부 가져오기.
     *
     * 앱 실행 시 생체인증을 요구할지 여부를 확인합니다.
     * MainActivity의 onResume()에서 호출되어 인증 필요 여부를 판단합니다.
     *
     * 기본값: true
     * - 최초 설치 시에는 보안을 위해 생체인증이 활성화되어 있습니다.
     * - 사용자가 명시적으로 비활성화하기 전까지는 인증이 필요합니다.
     *
     * @param context Context 인스턴스
     * @return 생체인증 활성화 여부
     *         - true: 생체인증 활성화됨 (기본값)
     *         - false: 사용자가 명시적으로 비활성화함
     */
    fun isBiometricEnabled(context: Context): Boolean {
        val enabled = getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, true)
        Timber.d("isBiometricEnabled: enabled=$enabled")
        return enabled
    }
}
