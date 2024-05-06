package com.luque.webauthn.data

data class PublicKeyCredentialParameters(
    val type: PublicKeyCredentialType = PublicKeyCredentialType.PublicKey,
    var alg: Int
)

