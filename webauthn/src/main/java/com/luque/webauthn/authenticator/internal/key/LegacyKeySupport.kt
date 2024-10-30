package com.luque.webauthn.authenticator.internal.key

import android.content.Context
import android.security.KeyPairGeneratorSpec
import com.luque.webauthn.authenticator.attestation.AttestationObject
import com.luque.webauthn.authenticator.AuthenticatorData
import com.luque.webauthn.authenticator.COSEKey
import com.luque.webauthn.authenticator.COSEKeyCurveType
import com.luque.webauthn.authenticator.COSEKeyEC2
import com.luque.webauthn.error.InvalidStateException
import com.luque.webauthn.util.WAKLogger
import com.luque.webauthn.util.ByteArrayUtil
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.*
import javax.security.auth.x500.X500Principal

class LegacyKeySupport(
    private val context: Context,
    override val alg: Int
) : KeySupport {

    companion object {
        val TAG = LegacyKeySupport::class.simpleName
    }

    override fun createKeyPair(alias: String, clientDataHash: ByteArray): COSEKey? {
        return try {
            val generator = KeyPairGenerator.getInstance("EC", KeyStoreType.Android)
            generator.initialize(createKeyPairSpec(alias))

            val pubKey = generator.generateKeyPair().public as ECPublicKey
            val encoded = pubKey.encoded.takeIf { it.size == 91 }
                ?: throw InvalidStateException("length of ECPublicKey should be 91")

            val x = encoded.copyOfRange(27, 59)
            val y = encoded.copyOfRange(59, 91)

            COSEKeyEC2(
                alg = alg,
                crv = COSEKeyCurveType.p256,
                x = x,
                y = y
            )
        } catch (e: Exception) {
            WAKLogger.w(TAG, "Failed to create key pair: ${e.localizedMessage}")
            null
        }
    }

    private fun createKeyPairSpec(alias: String): KeyPairGeneratorSpec {
        val endCalendar = Calendar.getInstance().apply { add(Calendar.YEAR, 100) }
        return KeyPairGeneratorSpec.Builder(context)
            .setAlgorithmParameterSpec(ECGenParameterSpec(CurveType.SECP256r1))
            .setAlias(alias)
            .setSubject(X500Principal("CN=$alias"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(Date())
            .setEndDate(endCalendar.time)
            .build()
    }

    override fun sign(alias: String, data: ByteArray): ByteArray? {
        return try {
            val keyStore = KeyStore.getInstance(KeyStoreType.Android).apply { load(null) }
            val privateKey = keyStore.getKey(alias, null) as PrivateKey
            Signature.getInstance(SignAlgorithmType.SHA256WithECDSA).run {
                initSign(privateKey)
                update(data)
                sign()
            }
        } catch (e: Exception) {
            WAKLogger.w(TAG, "Failed to sign data: ${e.localizedMessage}")
            null
        }
    }

    override fun buildAttestationObject(
        alias: String,
        clientDataHash: ByteArray,
        authenticatorData: AuthenticatorData
    ): AttestationObject? {
        val authDataBytes = authenticatorData.toBytes() ?: run {
            WAKLogger.d(TAG, "Failed to build authenticator data")
            return null
        }

        val bytesToBeSigned = ByteArrayUtil.merge(authDataBytes, clientDataHash)
        val signature = sign(alias, bytesToBeSigned) ?: run {
            WAKLogger.d(TAG, "Failed to sign authenticator data")
            return null
        }

        val attStmt = mutableMapOf<String, Any>(
            "alg" to alg.toLong(),
            "sig" to signature
        )

        return AttestationObject(
            fmt = AttestationFormatType.Packed,
            authData = authenticatorData,
            attStmt = attStmt
        )
    }
}
