package com.luque.webauthn.data

data class TokenBinding(
    var status: TokenBindingStatus,
    var id: String
)

