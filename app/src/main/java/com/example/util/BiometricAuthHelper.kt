package com.example.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthHelper {

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        NO_HARDWARE,
        HW_UNAVAILABLE,
        UNSUPPORTED
    }

    fun checkBiometricStatus(context: Context): Pair<BiometricStatus, String> {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Pair(BiometricStatus.AVAILABLE, "البصمة الحيوية متوفرة وجاهزة للاستخدام")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Pair(BiometricStatus.NOT_ENROLLED, "الجهاز يدعم البصمة ولكن لم يتم تسجيل أي بصمة بعد في إعدادات النظام")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Pair(BiometricStatus.NO_HARDWARE, "الجهاز لا يحتوي على مستشعر بصمة أصبع أو بصمة وجه")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Pair(BiometricStatus.HW_UNAVAILABLE, "مستشعر البصمة غير متاح حالياً، يرجى المحاولة لاحقاً")
            }
            else -> {
                Pair(BiometricStatus.UNSUPPORTED, "خاصية الدخول بالبصمة غير مدعومة على هذا الجهاز")
            }
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "تسجيل الدخول بالبصمة",
        subtitle: String = "تأكيد الهوية للوصول إلى نظام كروت نت",
        negativeButtonText: String = "استخدام رمز المرور PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val (status, message) = checkBiometricStatus(activity)
        if (status != BiometricStatus.AVAILABLE) {
            onError(message)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Do not report cancellation as an error requiring an alert
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("لم يتم التعرف على البصمة، يرجى إعادة المحاولة")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
