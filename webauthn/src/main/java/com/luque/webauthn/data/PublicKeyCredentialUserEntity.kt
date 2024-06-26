package com.luque.webauthn.data

data class PublicKeyCredentialUserEntity(
    var id: ByteArray = byteArrayOf(),
    var name: String = "",
    var displayName: String = "",
    var icon: String? = null
)

