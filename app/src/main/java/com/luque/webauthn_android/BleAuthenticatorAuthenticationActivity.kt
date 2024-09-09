package com.luque.webauthn_android

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class BleAuthenticatorAuthenticationActivity : AppCompatActivity() {

    companion object {
        val TAG = BleAuthenticatorAuthenticationActivity::class.simpleName
    }

    private lateinit var startButton: Button
    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_authenticator_authentication) // Define o layout XML

        title = "AUTHENTICATION BLE SERVICE"

        // Inicialize os botões
        startButton = findViewById(R.id.start_button)
        closeButton = findViewById(R.id.close_button)

        // Configura o clique do botão START
        startButton.setOnClickListener {
            onStartButtonClicked()
        }

        // Configura o clique do botão CLOSE
        closeButton.setOnClickListener {
            onCloseButtonClicked()
        }
    }

    private fun onStartButtonClicked() {
        // Lógica para o botão START
    }

    private fun onCloseButtonClicked() {
        finish()
    }
}
