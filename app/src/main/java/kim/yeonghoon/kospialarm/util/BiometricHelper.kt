package kim.yeonghoon.kospialarm.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

/**
 * 생체인증 헬퍼 클래스.
 *
 * Android Biometric API를 사용하여 지문, 얼굴 등의 생체인증 기능을 제공합니다.
 * 이 클래스는 다음 기능을 포함합니다:
 * - 디바이스의 생체인증 가능 여부 확인
 * - 생체인증 프롬프트 표시 및 결과 처리
 *
 * 보안 수준: BIOMETRIC_STRONG (강력한 생체인증만 허용)
 * - Class 3 생체인증 (지문, 홍채, 3D 얼굴인식)
 * - 스푸핑 공격에 대한 높은 저항성
 */
object BiometricHelper {

    /**
     * 생체인증 가능 여부 확인.
     *
     * 디바이스에 생체인증 하드웨어가 있는지, 사용 가능한지,
     * 그리고 등록된 생체인증 정보가 있는지 확인합니다.
     *
     * @param context Context 인스턴스
     * @return 생체인증 사용 가능 여부
     *         - true: 생체인증을 사용할 수 있음
     *         - false: 하드웨어 없음, 사용 불가, 또는 등록된 생체정보 없음
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)

        // BIOMETRIC_STRONG: Class 3 생체인증만 허용 (가장 높은 보안 수준)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // 생체인증 하드웨어가 있고, 사용 가능하며, 등록된 생체정보가 있음
                Timber.d("canAuthenticate: 생체인증 사용 가능")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // 디바이스에 생체인증 하드웨어가 없음 (예: 저가형 기기)
                Timber.w("canAuthenticate: 생체인증 하드웨어 없음")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // 하드웨어는 있지만 현재 사용할 수 없음 (일시적 오류)
                Timber.w("canAuthenticate: 생체인증 하드웨어 사용 불가")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // 하드웨어는 있지만 사용자가 생체정보를 등록하지 않음
                Timber.w("canAuthenticate: 등록된 생체인증 없음")
                false
            }
            else -> {
                // 기타 알 수 없는 오류
                Timber.w("canAuthenticate: 알 수 없는 오류")
                false
            }
        }
    }

    /**
     * 생체인증 프롬프트 표시.
     *
     * 사용자에게 생체인증 프롬프트를 표시하고 결과를 처리합니다.
     * 프롬프트는 시스템 UI로 표시되며, 앱이 직접 생체정보를 처리하지 않습니다.
     *
     * 인증 결과:
     * - 성공: onSuccess 콜백 호출, 사용자가 앱에 접근할 수 있음
     * - 실패: onAuthenticationFailed 호출, 사용자가 다시 시도할 수 있음
     * - 에러: onError 콜백 호출 (취소, 타임아웃, 하드웨어 오류 등)
     *
     * 사용자 취소 처리:
     * - ERROR_NEGATIVE_BUTTON: "취소" 버튼 클릭 (앱 종료하지 않음)
     * - ERROR_USER_CANCELED: 뒤로가기 또는 외부 영역 터치 (앱 종료하지 않음)
     * - 기타 에러: onError 콜백 호출하여 앱 종료 처리
     *
     * @param activity FragmentActivity 인스턴스 (BiometricPrompt는 FragmentActivity 필요)
     * @param onSuccess 인증 성공 시 호출되는 콜백
     * @param onError 인증 에러 시 호출되는 콜백 (errorCode, errorMessage)
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit
    ) {
        Timber.d("showBiometricPrompt: 생체인증 프롬프트 표시")

        // 메인 스레드에서 UI 업데이트를 위한 Executor
        val executor = ContextCompat.getMainExecutor(activity)

        // BiometricPrompt 인스턴스 생성 및 콜백 설정
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                /**
                 * 인증 에러 발생 시 호출.
                 *
                 * 사용자 취소(NEGATIVE_BUTTON, USER_CANCELED)는 정상적인 흐름이므로
                 * 에러로 처리하지 않고, 기타 에러만 onError 콜백으로 전달합니다.
                 */
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.e("onAuthenticationError: errorCode=$errorCode, message=$errString")

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // 사용자가 "취소" 버튼을 눌렀을 때
                            // 앱을 종료하지 않고 프롬프트만 닫음
                            Timber.i("onAuthenticationError: 사용자가 취소함")
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            // 사용자가 뒤로 가기 또는 외부 영역을 터치하여 취소
                            // 앱을 종료하지 않고 프롬프트만 닫음
                            Timber.i("onAuthenticationError: 사용자가 취소함 (백버튼)")
                        }
                        else -> {
                            // 타임아웃, 하드웨어 오류 등의 심각한 에러
                            // MainActivity에서 finish()를 호출하여 앱 종료
                            onError(errorCode, errString.toString())
                        }
                    }
                }

                /**
                 * 인증 성공 시 호출.
                 *
                 * 생체정보가 시스템에 등록된 정보와 일치할 때 호출됩니다.
                 */
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Timber.i("onAuthenticationSucceeded: 생체인증 성공")
                    onSuccess()
                }

                /**
                 * 인증 실패 시 호출 (재시도 가능).
                 *
                 * 생체정보가 일치하지 않지만, 프롬프트는 계속 표시되어
                 * 사용자가 다시 시도할 수 있습니다.
                 */
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.w("onAuthenticationFailed: 생체인증 실패 (재시도 가능)")
                }
            }
        )

        // 프롬프트 UI 설정
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("KOSPI Alarm")                                                    // 프롬프트 제목
            .setSubtitle("지문을 인식해주세요")                                            // 프롬프트 부제목
            .setNegativeButtonText("취소")                                               // 취소 버튼 텍스트
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG) // 강력한 생체인증만 허용
            .build()

        // 생체인증 프롬프트 표시
        biometricPrompt.authenticate(promptInfo)
    }
}
