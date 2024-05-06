package com.luque.webauthn.data

import com.luque.webauthn.data.AttestationConveyancePreference

class PublicKeyCredentialCreationOptions(
    var rp: PublicKeyCredentialRpEntity = PublicKeyCredentialRpEntity(),
    var user: PublicKeyCredentialUserEntity = PublicKeyCredentialUserEntity(),
    var challenge: ByteArray = ByteArray(0),
    var pubKeyCredParams: MutableList<PublicKeyCredentialParameters> = ArrayList(),
    var timeout: Long? = null,
    var excludeCredentials: MutableList<PublicKeyCredentialDescriptor> = ArrayList(),
    var authenticatorSelection: AuthenticatorSelectionCriteria? = null,
    var attestation: AttestationConveyancePreference = AttestationConveyancePreference.Direct,
    var extensions: Map<String, Any> = HashMap()
) {
    fun addPubKeyCredParam(alg: Int) {
        this.pubKeyCredParams.add(PublicKeyCredentialParameters(alg = alg))
    }
}

