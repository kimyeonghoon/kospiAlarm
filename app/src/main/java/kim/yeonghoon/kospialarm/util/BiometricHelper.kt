package kim.yeonghoon.kospialarm.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

/**
 * 생체인증 헬퍼 클래스.
 */
object BiometricHelper {

    /**
     * 생체인증 가능 여부 확인.
     *
     * @param context Context
     * @return 생체인증 가능 여부
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("canAuthenticate: 생체인증 사용 가능")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Timber.w("canAuthenticate: 생체인증 하드웨어 없음")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Timber.w("canAuthenticate: 생체인증 하드웨어 사용 불가")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Timber.w("canAuthenticate: 등록된 생체인증 없음")
                false
            }
            else -> {
                Timber.w("canAuthenticate: 알 수 없는 오류")
                false
            }
        }
    }

    /**
     * 생체인증 프롬프트 표시.
     *
     * @param activity FragmentActivity
     * @param onSuccess 인증 성공 콜백
     * @param onError 인증 실패 콜백
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit
    ) {
        Timber.d("showBiometricPrompt: 생체인증 프롬프트 표시")

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.e("onAuthenticationError: errorCode=$errorCode, message=$errString")

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // 사용자가 취소 버튼을 눌렀을 때
                            Timber.i("onAuthenticationError: 사용자가 취소함")
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            // 사용자가 뒤로 가기 등으로 취소했을 때
                            Timber.i("onAuthenticationError: 사용자가 취소함 (백버튼)")
                        }
                        else -> {
                            onError(errorCode, errString.toString())
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Timber.i("onAuthenticationSucceeded: 생체인증 성공")
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.w("onAuthenticationFailed: 생체인증 실패 (재시도 가능)")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("KOSPI Alarm")
            .setSubtitle("지문을 인식해주세요")
            .setNegativeButtonText("취소")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
