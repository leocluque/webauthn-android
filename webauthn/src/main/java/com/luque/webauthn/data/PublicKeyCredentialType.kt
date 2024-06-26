package com.luque.webauthn.data

enum class PublicKeyCredentialType(
    private val rawValue: String
) {
    PublicKey("public-key");

    override fun toString(): String {
        return rawValue
    }
}

