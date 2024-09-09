package com.luque.webauthn_android

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.WAKLogger

class RegistrationResultActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegistrationResultActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_result)

        val credId = intent.getStringExtra("CRED_ID")
        val credRaw = intent.getStringExtra("CRED_RAW")
        val clientJSON = intent.getStringExtra("CLIENT_JSON")
        val attestation = intent.getStringExtra("ATTESTATION")

        val jsonBase64 = ByteArrayUtil.encodeBase64URL(clientJSON!!.toByteArray())
        WAKLogger.d(TAG, "CRED_ID:" + credId)
        WAKLogger.d(TAG, "CRED_RAW:" + credRaw)
        WAKLogger.d(TAG, "CLIENT_JSON:" + jsonBase64)

        var printed = 0
        val numForEachLine = 1000
        while (printed < attestation!!.length) {
            if (printed + numForEachLine < attestation.length) {
                WAKLogger.d(TAG, "ATTESTATION:" + attestation.substring(printed, printed + numForEachLine))
                printed += numForEachLine
            } else {
                WAKLogger.d(TAG, "ATTESTATION:" + attestation.substring(printed, attestation.length))
                break
            }
        }

        // Bind the views
        findViewById<EditText>(R.id.raw_id_field).setText(credRaw)
        findViewById<EditText>(R.id.cred_id_field).setText(credId)
        findViewById<EditText>(R.id.client_data_field).setText(clientJSON)
        findViewById<EditText>(R.id.attestation_field).setText(attestation)

        // Set up button click listener
        findViewById<Button>(R.id.close_button).setOnClickListener {
            onCloseButtonClicked()
        }
    }

    private fun onCloseButtonClicked() {
        finish()
    }
}
