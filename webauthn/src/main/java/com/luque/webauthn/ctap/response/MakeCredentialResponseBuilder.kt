package com.luque.webauthn.ctap.response

import com.luque.webauthn.authenticator.AttestationObject
import com.luque.webauthn.error.ErrorReason
import com.luque.webauthn.util.CBORWriter
import com.luque.webauthn.util.WAKLogger

@ExperimentalUnsignedTypes
class MakeCredentialResponseBuilder(
    private val attestationObject: AttestationObject
) {

    companion object {
        val TAG = MakeCredentialResponseBuilder::class.simpleName
    }

    fun build(): Pair<ByteArray?, ErrorReason?>  {
        WAKLogger.d(TAG, "build")

        return try {

            val map = mutableMapOf<String, Any>()

            map["attStmt"]  = attestationObject.attStmt
            map["fmt"]      = attestationObject.fmt
            map["authData"] = attestationObject.authData.toBytes()!!

            val result = CBORWriter().putStringKeyMap(map).compute()
            Pair(result, null)

        } catch (e: Exception) {

            WAKLogger.w(TAG, "failed to build CBOR: $e - ${e.message}")

            Pair(null, ErrorReason.Unknown)

        }

    }

}