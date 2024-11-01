package com.luque.webauthn.authenticator

import com.luque.webauthn.authenticator.attestation.AttestationObject
import com.luque.webauthn.data.AuthenticatorAttachment
import com.luque.webauthn.data.AuthenticatorTransport
import com.luque.webauthn.data.PublicKeyCredentialDescriptor
import com.luque.webauthn.data.PublicKeyCredentialParameters
import com.luque.webauthn.data.PublicKeyCredentialRpEntity
import com.luque.webauthn.data.PublicKeyCredentialUserEntity
import com.luque.webauthn.error.ErrorReason

class AuthenticatorAssertionResult(
    var credentialId: ByteArray?,
    var userHandle: ByteArray?,
    var signature: ByteArray,
    var authenticatorData: ByteArray,
)

interface MakeCredentialSessionListener {
    fun onAvailable(session: MakeCredentialSession) {}
    fun onUnavailable(session: MakeCredentialSession) {}
    fun onOperationStopped(session: MakeCredentialSession, reason: ErrorReason) {}
    fun onCredentialCreated(session: MakeCredentialSession, attestationObject: AttestationObject) {}
}

interface GetAssertionSessionListener {
    fun onAvailable(session: GetAssertionSession) {}
    fun onUnavailable(session: GetAssertionSession) {}
    fun onOperationStopped(session: GetAssertionSession, reason: ErrorReason) {}
    fun onCredentialDiscovered(
        session: GetAssertionSession,
        assertion: AuthenticatorAssertionResult,
    ) {
    }
}

interface GetAssertionSession {

    val attachment: AuthenticatorAttachment
    val transport: AuthenticatorTransport
    var listener: GetAssertionSessionListener?

    fun getAssertion(
        rpId: String,
        hash: ByteArray,
        allowCredentialDescriptorList: List<PublicKeyCredentialDescriptor>,
        requireUserPresence: Boolean,
        requireUserVerification: Boolean,
        // extensions: Map<String, Any>
    )

    fun canPerformUserVerification(): Boolean
    fun start()
    fun cancel(reason: ErrorReason)
}

interface MakeCredentialSession {

    val attachment: AuthenticatorAttachment
    val transport: AuthenticatorTransport
    var listener: MakeCredentialSessionListener?

    fun makeCredential(
        hash: ByteArray,
        rpEntity: PublicKeyCredentialRpEntity,
        userEntity: PublicKeyCredentialUserEntity,
        requireResidentKey: Boolean,
        requireUserPresence: Boolean,
        requireUserVerification: Boolean,
        credTypesAndPubKeyAlgs: List<PublicKeyCredentialParameters>,
        excludeCredentialDescriptorList: List<PublicKeyCredentialDescriptor>,
    )

    fun canPerformUserVerification(): Boolean
    fun canStoreResidentKey(): Boolean
    fun start()
    fun cancel(reason: ErrorReason)
}

interface Authenticator {

    val attachment: AuthenticatorAttachment
    val transport: AuthenticatorTransport
    val counterStep: UInt
    val allowResidentKey: Boolean
    val allowUserVerification: Boolean

    fun newMakeCredentialSession(): MakeCredentialSession
    fun newGetAssertionSession(): GetAssertionSession
}