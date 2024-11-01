package com.luque.webauthn.authenticator.internal.key

import com.luque.webauthn.authenticator.attestation.AttestationObject
import com.luque.webauthn.authenticator.AuthenticatorData
import com.luque.webauthn.authenticator.COSEKey

object KeyStoreType {
    const val Android = "AndroidKeyStore"
}

object CurveType {
    const val SECP256r1 = "secp256r1"
}

object SignAlgorithmType {
    const val SHA256WithRSA   = "SHA256withRSA"
    const val SHA256WithECDSA = "SHA256withECDSA"
}

object AttestationFormatType {
    const val AndroidKey = "android-key"
    const val Packed     = "packed"
}


interface KeySupport {
    val alg: Int
    fun createKeyPair(alias: String, clientDataHash: ByteArray): COSEKey?
    fun sign(alias: String, data: ByteArray): ByteArray?
    fun buildAttestationObject(
        alias:             String,
        clientDataHash:    ByteArray,
        authenticatorData: AuthenticatorData
    ): AttestationObject?
}

