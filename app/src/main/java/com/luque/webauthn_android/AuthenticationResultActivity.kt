package com.luque.webauthn_android

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.WAKLogger

class AuthenticationResultActivity : AppCompatActivity() {

    companion object {
        private val TAG = AuthenticationResultActivity::class.simpleName
    }

    private lateinit var rawIdField: EditText
    private lateinit var credIdField: EditText
    private lateinit var clientDataField: EditText
    private lateinit var authenticatorDataField: EditText
    private lateinit var signatureField: EditText
    private lateinit var userHandleField: EditText
    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_result) // Define o layout XML

        // Inicialize as views
        rawIdField = findViewById(R.id.raw_id_field)
        credIdField = findViewById(R.id.cred_id_field)
        clientDataField = findViewById(R.id.client_data_field)
        authenticatorDataField = findViewById(R.id.authenticator_data_field)
        signatureField = findViewById(R.id.signature_field)
        userHandleField = findViewById(R.id.user_handle_field)
        closeButton = findViewById(R.id.close_button)

        // Obtenha os dados da Intent
        val credId = intent.getStringExtra("CRED_ID")
        val credRaw = intent.getStringExtra("CRED_RAW")
        val clientJSON = intent.getStringExtra("CLIENT_JSON")
        val authenticatorData = intent.getStringExtra("AUTHENTICATOR_DATA")
        val signature = intent.getStringExtra("SIGNATURE")
        val userHandle = intent.getStringExtra("USER_HANDLE")

        // Atualize as views com os dados
        rawIdField.setText(credRaw)
        credIdField.setText(credId)
        clientDataField.setText(clientJSON)
        authenticatorDataField.setText(authenticatorData)
        signatureField.setText(signature)
        userHandleField.setText(userHandle)

        // Configura o botão de fechar
        closeButton.setOnClickListener {
            onCloseButtonClicked()
        }

        // Log para verificação
        val jsonBase64 = ByteArrayUtil.encodeBase64URL(clientJSON!!.toByteArray())
        WAKLogger.d(TAG, "CRED_ID:" + credId)
        WAKLogger.d(TAG, "CRED_RAW:" + credRaw)
        WAKLogger.d(TAG, "CLIENT_JSON:" + jsonBase64)
        WAKLogger.d(TAG, "AUTHENTICATOR_DATA:" + authenticatorData)
        WAKLogger.d(TAG, "signature:" + signature)
        WAKLogger.d(TAG, "userHandle:" + userHandle)
    }

    private fun onCloseButtonClicked() {
        finish()
    }
}
