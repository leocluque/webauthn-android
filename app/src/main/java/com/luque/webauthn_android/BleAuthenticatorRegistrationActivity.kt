package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luque.webauthn.authenticator.internal.ui.UserConsentUI
import com.luque.webauthn.authenticator.internal.ui.UserConsentUIFactory
import com.luque.webauthn.ctap.ble.BleFidoService
import com.luque.webauthn.ctap.ble.BleFidoServiceListener
import com.luque.webauthn.util.WAKLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class BleAuthenticatorRegistrationActivity : AppCompatActivity() {

    companion object {
        val TAG = BleAuthenticatorRegistrationActivity::class.simpleName
    }

    var consentUI: UserConsentUI? = null
    var bleFidoService: BleFidoService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "REGISTRATION BLE SERVICE"

//        verticalLayout {
//
//            padding = dip(10)
//
//            button("START") {
//                textSize = 24f
//
//                onClick {
//
//                    onStartClicked()
//
//                }
//
//            }
//
//            button("STOP") {
//                textSize = 24f
//
//                onClick {
//
//                    onStopClicked()
//
//                }
//            }
//
//        }
    }

    private fun onStartClicked() {
        WAKLogger.d(TAG, "onStartClicked")
        createBleFidoService()
        if (bleFidoService!!.start()) {
            WAKLogger.d(TAG, "started successfully")
        } else {
            WAKLogger.d(TAG, "failed to start")
        }
    }

    private fun onStopClicked() {
        WAKLogger.d(TAG, "onStopClicked")
        bleFidoService?.stop()
        bleFidoService = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        WAKLogger.d(TAG, "onActivityResult")
        consentUI?.onActivityResult(requestCode, resultCode, data)
        /*
        if (consentUI != null && consentUI!!.onActivityResult(requestCode, resultCode, data)) {
            return
        }
        */
    }

    val bleServiceListener = object: BleFidoServiceListener {

        override fun onConnected(address: String) {
            WAKLogger.d(TAG, "onConnected")
        }

        override fun onDisconnected(address: String) {
            WAKLogger.d(TAG, "onDisconnected")
        }

        override fun onClosed() {
            WAKLogger.d(TAG, "onClosed")
        }
    }


    private fun createBleFidoService() {

        consentUI = UserConsentUIFactory.create(this)

        bleFidoService = BleFidoService.create(
            activity  = this,
            ui        = consentUI!!,
            listener  = bleServiceListener
        )
    }

}
