package com.luque.webauthn.authenticator.internal.key

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import com.luque.webauthn.authenticator.AuthenticatorData
import com.luque.webauthn.authenticator.COSEKey
import com.luque.webauthn.authenticator.COSEKeyCurveType
import com.luque.webauthn.authenticator.COSEKeyEC2
import com.luque.webauthn.authenticator.attestation.AttestationObject
import com.luque.webauthn.error.InvalidStateException
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.WAKLogger
import java.io.Serializable
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class DefaultKeySupport(
    override val alg: Int
) : KeySupport {

    companion object {
        val TAG = DefaultKeySupport::class.simpleName
    }

    override fun createKeyPair(alias: String, clientDataHash: ByteArray): COSEKey? {
        return try {
            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                KeyStoreType.Android
            )
            generator.initialize(createGenParameterSpec(alias, clientDataHash))

            val pubKey = generator.generateKeyPair().public as? ECPublicKey
                ?: throw InvalidStateException("Generated public key is not an ECPublicKey")

            val encoded = pubKey.encoded
            require(encoded.size == 91) { "length of ECPublicKey should be 91" }

            val x = encoded.sliceArray(27 until 59)
            val y = encoded.sliceArray(59 until 91)

            COSEKeyEC2(alg, COSEKeyCurveType.p256, x, y)
        } catch (e: Exception) {
            WAKLogger.w(TAG, "failed to create key pair: ${e.localizedMessage}")
            null
        }
    }

    private fun createGenParameterSpec(alias: String, clientDataHash: ByteArray): KeyGenParameterSpec {
        val specBuilder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(ECGenParameterSpec(CurveType.SECP256r1))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(false)

        return specBuilder.setAttestationChallenge(clientDataHash).build()
    }

    override fun sign(alias: String, data: ByteArray): ByteArray? {
        return try {
            val keyStore = KeyStore.getInstance(KeyStoreType.Android).apply { load(null) }
            val privateKey = keyStore.getKey(alias, null) as PrivateKey
            Signature.getInstance(SignAlgorithmType.SHA256WithECDSA).apply {
                initSign(privateKey)
                update(data)
            }.sign()
        } catch (e: Exception) {
            WAKLogger.w(TAG, "Failed to sign data: ${e.localizedMessage}")
            null
        }
    }

    private fun useSecureHardware(alias: String): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KeyStoreType.Android).apply { load(null) }
            val key = keyStore.getKey(alias, null)
            val factory = KeyFactory.getInstance(key.algorithm, KeyStoreType.Android)
            val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            keyInfo.isInsideSecureHardware
        } catch (e: Exception) {
            WAKLogger.w(TAG, "Failed to verify secure hardware: ${e.localizedMessage}")
            false
        }
    }

    override fun buildAttestationObject(
        alias: String,
        clientDataHash: ByteArray,
        authenticatorData: AuthenticatorData
    ): AttestationObject? {

        val authenticatorDataBytes = authenticatorData.toBytes()
            ?: run {
                WAKLogger.d(TAG, "Failed to build authenticator data")
                return null
            }

        val bytesToBeSigned = ByteArrayUtil.merge(authenticatorDataBytes, clientDataHash)
        val sig = sign(alias, bytesToBeSigned)
            ?: run {
                WAKLogger.d(TAG, "Failed to sign authenticator data")
                return null
            }

        val attStmt: MutableMap<String, Serializable> = mutableMapOf(
            "alg" to alg.toLong(),
            "sig" to sig
        )

        return if (useSecureHardware(alias)) {
            WAKLogger.d(TAG, "Using 'attestation-key' format due to secure hardware support")
            val certs = KeyStore.getInstance(KeyStoreType.Android).apply { load(null) }
                .getCertificateChain(alias)
                ?.map { (it as X509Certificate).encoded }
                ?.map { Base64.encodeToString(it, Base64.NO_WRAP) } // Converte os certificados para Base64

            attStmt["x5c"] = certs as Serializable // Certificados em Base64 são agora serializáveis

            AttestationObject(
                fmt = AttestationFormatType.AndroidKey,
                authData = authenticatorData,
                attStmt = attStmt
            )
        } else {
            WAKLogger.d(TAG, "Using self-attestation format as secure hardware is unsupported")
            AttestationObject(
                fmt = AttestationFormatType.Packed,
                authData = authenticatorData,
                attStmt = attStmt
            )
        }
    }
}
