package com.luque.webauthn.authenticator.internal

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.luque.webauthn.util.WAKLogger
import com.luque.webauthn.authenticator.Authenticator
import com.luque.webauthn.authenticator.GetAssertionSession
import com.luque.webauthn.authenticator.MakeCredentialSession
import com.luque.webauthn.authenticator.internal.key.KeySupportChooser
import com.luque.webauthn.authenticator.internal.session.InternalGetAssertionSession
import com.luque.webauthn.authenticator.internal.session.InternalMakeCredentialSession
import com.luque.webauthn.authenticator.internal.ui.UserConsentUI
import com.luque.webauthn.data.AuthenticatorAttachment
import com.luque.webauthn.data.AuthenticatorTransport


class InternalAuthenticatorSetting {
    val attachment = AuthenticatorAttachment.Platform
    val transport  = AuthenticatorTransport.Internal
    var counterStep: UInt = 1u
    var allowUserVerification = true
}



class InternalAuthenticator(
    private val activity:          FragmentActivity,
    private val ui: UserConsentUI,
    private val credentialStore: CredentialStore = CredentialStore(activity),
    private val keySupportChooser: KeySupportChooser = KeySupportChooser(activity)
) : Authenticator {

    companion object {
        val TAG = InternalAuthenticator::class.simpleName
    }

    private val setting = InternalAuthenticatorSetting()

    override val attachment: AuthenticatorAttachment
        get() = setting.attachment

    override val transport: AuthenticatorTransport
        get() = setting.transport

    override var counterStep: UInt
        get() = setting.counterStep
        set(value) { setting.counterStep = value }

    override val allowResidentKey: Boolean = true

    override var allowUserVerification: Boolean
        get() = setting.allowUserVerification
        set(value) { setting.allowUserVerification = value }

    override fun newGetAssertionSession(): GetAssertionSession {
        WAKLogger.d(TAG, "newGetAssertionSession")
        return InternalGetAssertionSession(
            setting           = setting,
            ui                = ui,
            credentialStore   = credentialStore,
            keySupportChooser = keySupportChooser
        )
    }

    override fun newMakeCredentialSession(): MakeCredentialSession {
        WAKLogger.d(TAG, "newMakeCredentialSession")
        return InternalMakeCredentialSession(
            setting           = setting,
            ui                = ui,
            credentialStore   = credentialStore,
            keySupportChooser = keySupportChooser
        )
    }


}