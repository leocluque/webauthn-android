package com.luque.webauthn.authenticator.internal.key

import android.content.Context
import com.luque.webauthn.authenticator.COSEAlgorithmIdentifier
import com.luque.webauthn.util.WAKLogger

class KeySupportChooser(private val context: Context) {

    companion object {
        val TAG = KeySupportChooser::class.simpleName
    }

    fun choose(algorithms: List<Int>): KeySupport? {
        WAKLogger.d(TAG, "choose support module")
        return chooseInternal(algorithms)
    }

    private fun chooseInternal(algorithms: List<Int>): KeySupport? {
        for (alg in algorithms) {
            when (alg) {
                COSEAlgorithmIdentifier.es256 -> {
                    return DefaultKeySupport(alg)
                }

                else -> {
                    WAKLogger.d(TAG, "key support for this algorithm not found")
                }
            }
        }
        WAKLogger.w(TAG, "no proper support module found")
        return null
    }

    private fun chooseLegacyInternal(algs: List<Int>): KeySupport? {
        for (alg in algs) {
            when (alg) {
                COSEAlgorithmIdentifier.es256 -> {
                    return LegacyKeySupport(context, alg)
                }

                else -> {
                    WAKLogger.d(TAG, "key support for this algorithm not found")
                }
            }
        }
        WAKLogger.w(TAG, "no proper support module found")
        return null
    }
}

