package com.luque.webauthn.authenticator.internal.ui

import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.luque.webauthn.authenticator.internal.PublicKeyCredentialSource
import com.luque.webauthn.authenticator.internal.ui.dialog.DefaultRegistrationConfirmationDialog
import com.luque.webauthn.authenticator.internal.ui.dialog.DefaultSelectionConfirmationDialog
import com.luque.webauthn.authenticator.internal.ui.dialog.RegistrationConfirmationDialogListener
import com.luque.webauthn.authenticator.internal.ui.dialog.SelectionConfirmationDialogListener
import com.luque.webauthn.authenticator.internal.ui.dialog.VerificationErrorDialog
import com.luque.webauthn.authenticator.internal.ui.dialog.VerificationErrorDialogListener
import com.luque.webauthn.data.PublicKeyCredentialRpEntity
import com.luque.webauthn.data.PublicKeyCredentialUserEntity
import com.luque.webauthn.error.CancelledException
import com.luque.webauthn.error.ErrorReason
import com.luque.webauthn.util.WAKLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine



@TargetApi(Build.VERSION_CODES.M)
class DefaultUserConsentUI(
    private val activity: FragmentActivity
) : UserConsentUI {

    companion object {
        val TAG = DefaultUserConsentUI::class.simpleName
        const val REQUEST_CODE = 6749
    }

    var keyguardResultListener: KeyguardResultListener? = null

    override val config = UserConsentUIConfig()
    var teste : (() -> Unit)? = null

    override var isOpen: Boolean = false
        private set

    private var cancelled: ErrorReason? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        WAKLogger.d(TAG, "onActivityResult")
        return if (requestCode == REQUEST_CODE) {
            WAKLogger.d(TAG, "This is my result")
            keyguardResultListener?.let {
                if (resultCode == RESULT_OK) {
                    WAKLogger.d(TAG, "OK")
                    it.onAuthenticated()
                } else {
                    WAKLogger.d(TAG, "Failed")
                    it.onFailed()
                }
            }
            keyguardResultListener = null
            true
        } else {
            if (requestCode == 1408) {
                if (resultCode == RESULT_OK) {
                    CancellationSignal()
                    // Autenticação foi bem-sucedida
                    teste?.invoke()
                } else {
                    // Falha na autenticação
//                    showErrorDialog(cont, "Falha na autenticação com bloqueio de tela.")
                }
                true
            } else {
                false
            }
        }
    }

    private fun onStartUserInteraction() {
        isOpen = true
        cancelled = null
    }

    private fun <T> finish(cont: Continuation<T>, result: T) {
        WAKLogger.d(TAG, "finish")
        isOpen = false
        if (cancelled != null) {
            cont.resumeWithException(cancelled!!.rawValue)
        } else {
            cont.resume(result)
        }
    }

    private fun <T> fail(cont: Continuation<T>) {
        WAKLogger.d(TAG, "fail")
        isOpen = false
        if (cancelled != null) {
            cont.resumeWithException(cancelled!!.rawValue)
        } else {
            cont.resumeWithException(CancelledException())
        }
    }

    override fun cancel(reason: ErrorReason) {
        cancelled = reason
    }

    override suspend fun requestUserConsent(
        rpEntity: PublicKeyCredentialRpEntity,
        userEntity: PublicKeyCredentialUserEntity,
        requireUserVerification: Boolean
    ): String = suspendCoroutine { cont ->
        WAKLogger.d(TAG, "requestUserConsent")
        onStartUserInteraction()

        activity.runOnUiThread {
            WAKLogger.d(TAG, "requestUserConsent switched to UI thread")
            val dialog = DefaultRegistrationConfirmationDialog(config)
            dialog.show(activity, rpEntity, userEntity, object :
                RegistrationConfirmationDialogListener {

                override fun onCreate(keyName: String) {
                    if (requireUserVerification) {
                        showBiometricPrompt(cont, keyName)
                    } else {
                        finish(cont, keyName)
                    }
                }

                override fun onCancel() {
                    fail(cont)
                }
            })
        }
    }

    override suspend fun requestUserSelection(
        sources: List<PublicKeyCredentialSource>,
        requireUserVerification: Boolean
    ): PublicKeyCredentialSource = suspendCoroutine { cont ->
        WAKLogger.d(TAG, "requestUserSelection")
        onStartUserInteraction()

        activity.runOnUiThread {
            if (sources.size == 1 && !config.alwaysShowKeySelection) {
                WAKLogger.d(TAG, "found 1 source, skip selection")
                executeSelectionVerificationIfNeeded(requireUserVerification, sources[0], cont)
            } else {
                WAKLogger.d(TAG, "show selection dialog")
                val dialog = DefaultSelectionConfirmationDialog(config)
                dialog.show(activity, sources, object : SelectionConfirmationDialogListener {
                    override fun onSelect(source: PublicKeyCredentialSource) {
                        WAKLogger.d(TAG, "selected")
                        executeSelectionVerificationIfNeeded(requireUserVerification, source, cont)
                    }

                    override fun onCancel() {
                        WAKLogger.d(TAG, "canceled")
                        fail(cont)
                    }
                })
            }
        }
    }

    private fun executeSelectionVerificationIfNeeded(
        requireUserVerification: Boolean,
        source: PublicKeyCredentialSource,
        cont: Continuation<PublicKeyCredentialSource>
    ) {
        if (requireUserVerification) {
            showBiometricPrompt(cont, source)
        } else {
            finish(cont, source)
        }
    }

    private fun <T> showBiometricPrompt(cont: Continuation<T>, consentResult: T) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showBiometricPromptApi29AndAbove(cont, consentResult)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFingerprintPromptApi23To27(cont, consentResult)
        } else {
            fail(cont) // Fail if the device is running an unsupported version
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun <T> showBiometricPromptApi29AndAbove(cont: Continuation<T>, consentResult: T) {
        val builder = BiometricPrompt.Builder(activity)
            .setTitle("Autenticação Biométrica")
            .setSubtitle("Use sua impressão digital para autenticar")
            .setDescription("Coloque o dedo no sensor de impressão digital")

        if (config.useOnlyFingerprint) {
            // Apenas impressões digitais permitidas, então não adicionamos o botão negativo
            builder.setDeviceCredentialAllowed(false)
            builder.setNegativeButton("Cancelar", ContextCompat.getMainExecutor(activity)) { _, _ ->
                fail(cont)
            }
        } else {
            // Permite PIN, senha ou impressões digitais
            builder.setDeviceCredentialAllowed(true)

        }

        val biometricPrompt = builder.build()

        biometricPrompt.authenticate(
            CancellationSignal(),
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    finish(cont, consentResult)
                }

                override fun onAuthenticationFailed() {
                    showErrorDialog(cont, "Falha na autenticação biométrica.")
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun <T> showFingerprintPromptApi23To27(cont: Continuation<T>, consentResult: T) {

        try {
            val fingerprintManager = activity.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            if (!fingerprintManager.isHardwareDetected || !fingerprintManager.hasEnrolledFingerprints()) {
                showErrorDialog(cont, "Fingerprint authentication is not set up.")
                return
            }
            val cancellationSignal = CancellationSignal()
            fingerprintManager.authenticate(
                null,
                cancellationSignal,
                0,
                object : FingerprintManager.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                        finish(cont, consentResult)
                    }

                    override fun onAuthenticationFailed() {
                        showErrorDialog(cont, "Falha na autenticação com impressão digital.")
                    }
                },
                null
            )
        } catch (e: Exception) {
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isKeyguardSecure) {
                val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Autenticação necessária",
                    "Autentique-se com seu PIN, padrão ou senha."
                )

                if (intent != null) {
                    activity.startActivityForResult(intent, 1408)
                     teste = {
                         finish(cont, consentResult)
                     }
                } else {
                    // Caso o intent não possa ser criado (muito raro)
                    showErrorDialog(cont, "Não foi possível solicitar autenticação.")
                }
            } else {
                // Bloqueio de tela não está configurado
                showErrorDialog(cont, "Nenhum método de bloqueio de tela configurado.")
            }
        }

    }

    private fun <T> showErrorDialog(cont: Continuation<T>, reason: String) {
        val dialog = VerificationErrorDialog(config)
        dialog.show(activity, reason, object : VerificationErrorDialogListener {
            override fun onComplete() {
                fail(cont)
            }
        })
    }
}
