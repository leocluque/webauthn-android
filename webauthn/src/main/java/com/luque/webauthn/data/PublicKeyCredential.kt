package com.luque.webauthn.data

data class PublicKeyCredential<T: AuthenticatorResponse>(
    val type: PublicKeyCredentialType = PublicKeyCredentialType.PublicKey,
    var id: String,
    var rawId: ByteArray,
    var response: T
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKeyCredential<*>

        if (type != other.type) return false
        if (id != other.id) return false
        if (!rawId.contentEquals(other.rawId)) return false
        if (response != other.response) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + rawId.contentHashCode()
        result = 31 * result + response.hashCode()
        return result
    }
}

