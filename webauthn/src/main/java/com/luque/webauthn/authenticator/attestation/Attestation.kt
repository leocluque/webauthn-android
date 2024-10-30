package com.luque.webauthn.authenticator.attestation

import com.luque.webauthn.authenticator.AuthenticatorData
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.CBORWriter
import com.luque.webauthn.util.WAKLogger

class AttestationObject(
    val fmt: String,
    val authData: AuthenticatorData,
    val attStmt: Map<String, Any>
) {

    companion object {
        val TAG = AttestationObject::class.simpleName
    }

    fun toNone(): AttestationObject = AttestationObject("none", this.authData, emptyMap())

    fun isSelfAttestation(): Boolean {
        WAKLogger.d(TAG, "isSelfAttestation")
        return fmt == "packed" && !attStmt.containsKey("x5c") &&
                !attStmt.containsKey("ecdaaKeyId") &&
                authData.attestedCredentialData == null &&
                authData.attestedCredentialData?.aaguid?.all { it == 0x00.toByte() } == true
    }

    fun toBytes(): ByteArray? {
        WAKLogger.d(TAG, "toBytes")

        val authDataBytes = authData.toBytes()
        if (authDataBytes == null) {
            WAKLogger.d(TAG, "failed to build authenticator data")
            return null
        }

        val map = mutableMapOf<String, Any>(
            "authData" to authDataBytes,
            "fmt" to fmt,
            "attStmt" to attStmt
        )

        WAKLogger.d(TAG, "AUTH_DATA: ${ByteArrayUtil.toHex(authDataBytes)}")

        return try {
            CBORWriter().putStringKeyMap(map).compute()
        } catch (e: Exception) {
            WAKLogger.d(TAG, "failed to build attestation binary: ${e.localizedMessage}")
            null
        }
    }
}
