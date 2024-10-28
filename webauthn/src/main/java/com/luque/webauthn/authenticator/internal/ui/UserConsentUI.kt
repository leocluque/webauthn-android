package com.luque.webauthn.authenticator.internal.ui

import android.content.Intent
import com.luque.webauthn.authenticator.internal.PublicKeyCredentialSource
import com.luque.webauthn.data.PublicKeyCredentialRpEntity
import com.luque.webauthn.data.PublicKeyCredentialUserEntity
import com.luque.webauthn.error.ErrorReason

interface KeyguardResultListener {
    fun onAuthenticated()
    fun onFailed()
}

interface UserConsentUI {

    val config: UserConsentUIConfig

    val isOpen: Boolean

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean

    fun cancel(reason: ErrorReason)

    suspend fun requestUserConsent(
        rpEntity: PublicKeyCredentialRpEntity,
        userEntity: PublicKeyCredentialUserEntity,
        requireUserVerification: Boolean
    ): String

    suspend fun requestUserSelection(
        sources:                 List<PublicKeyCredentialSource>,
        requireUserVerification: Boolean
    ): PublicKeyCredentialSource

}

