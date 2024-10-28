package com.luque.webauthn.data

open class AuthenticatorResponse

data class AuthenticatorAttestationResponse(
    var clientDataJSON:    String,
    var attestationObject: ByteArray
): AuthenticatorResponse() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthenticatorAttestationResponse

        if (clientDataJSON != other.clientDataJSON) return false
        if (!attestationObject.contentEquals(other.attestationObject)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientDataJSON.hashCode()
        result = 31 * result + attestationObject.contentHashCode()
        return result
    }
}

data class AuthenticatorAssertionResponse(
    var clientDataJSON:    String,
    var authenticatorData: ByteArray,
    var signature:         ByteArray,
    var userHandle:        ByteArray?
): AuthenticatorResponse() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuthenticatorAssertionResponse

        if (clientDataJSON != other.clientDataJSON) return false
        if (!authenticatorData.contentEquals(other.authenticatorData)) return false
        if (!signature.contentEquals(other.signature)) return false
        if (userHandle != null) {
            if (other.userHandle == null) return false
            if (!userHandle.contentEquals(other.userHandle)) return false
        } else if (other.userHandle != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientDataJSON.hashCode()
        result = 31 * result + authenticatorData.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + (userHandle?.contentHashCode() ?: 0)
        return result
    }
}

