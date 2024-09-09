package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    private var consentUI: UserConsentUI? = null
    private var bleFidoService: BleFidoService? = null
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_authenticator_registration) // Define o layout XML

        title = "REGISTRATION BLE SERVICE"

        // Inicialize os botões
        startButton = findViewById(R.id.start_button)
        stopButton = findViewById(R.id.stop_button)

        // Configura o clique do botão START
        startButton.setOnClickListener {
            onStartClicked()
        }

        // Configura o clique do botão STOP
        stopButton.setOnClickListener {
            onStopClicked()
        }
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
    }

    private val bleServiceListener = object : BleFidoServiceListener {
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
            activity = this,
            ui = consentUI!!,
            listener = bleServiceListener
        )
    }
}
