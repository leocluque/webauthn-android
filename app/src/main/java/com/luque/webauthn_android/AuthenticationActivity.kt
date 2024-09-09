package com.luque.webauthn_android

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.luque.webauthn.authenticator.internal.ui.UserConsentUI
import com.luque.webauthn.authenticator.internal.ui.UserConsentUIFactory
import com.luque.webauthn.client.WebAuthnClient
import com.luque.webauthn.data.AuthenticatorTransport
import com.luque.webauthn.data.GetAssertionResponse
import com.luque.webauthn.data.PublicKeyCredentialRequestOptions
import com.luque.webauthn.data.UserVerificationRequirement
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.WAKLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        private val TAG = AuthenticationActivity::class.simpleName
    }

    private lateinit var relyingPartyField: EditText
    private lateinit var challengeField: EditText
    private lateinit var credIdField: EditText
    private lateinit var userVerificationSpinner: Spinner

    private val userVerificationOptions = listOf("Required", "Preferred", "Discouraged")

    override fun onCreate(savedInstanceState: Bundle?) {
        WAKLogger.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth_activity)

        title = "AUTHENTICATION"

        // Inicialize as views
        relyingPartyField = findViewById(R.id.relying_party_field)
        challengeField = findViewById(R.id.challenge_field)
        credIdField = findViewById(R.id.cred_id_field)
        userVerificationSpinner = findViewById(R.id.user_verification_spinner)

        // Configura o adapter do Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userVerificationOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userVerificationSpinner.adapter = adapter

        // Inicializa o WebAuthnClient
        webAuthnClient = createWebAuthnClient()
    }

    private fun onStartClicked() {
        val relyingParty = relyingPartyField.text.toString()
        val credId = credIdField.text.toString()
        val challenge = challengeField.text.toString()

        val userVerification = when (userVerificationSpinner.selectedItem.toString()) {
            "Required" -> UserVerificationRequirement.Required
            "Preferred" -> UserVerificationRequirement.Preferred
            "Discouraged" -> UserVerificationRequirement.Discouraged
            else -> UserVerificationRequirement.Preferred
        }

        lifecycleScope.launch {
            onExecute(
                relyingParty = relyingParty,
                challenge = challenge,
                credId = credId,
                userVerification = userVerification
            )
        }
    }

    private fun createWebAuthnClient(): WebAuthnClient {
        consentUI = UserConsentUIFactory.create(this)
        return WebAuthnClient.create(
            activity = this,
            origin = "https://example.org",
            ui = consentUI!!
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        WAKLogger.d(TAG, "onActivityResult")
        consentUI?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        WAKLogger.d(TAG, "onStart")
        super.onStart()
    }

    override fun onStop() {
        WAKLogger.d(TAG, "onStop")
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.authentication_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.webauthn_authentication_start) {
            onStartClicked()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private var consentUI: UserConsentUI? = null
    private var webAuthnClient: WebAuthnClient? = null

    private suspend fun onExecute(relyingParty: String, challenge: String,
                                  credId: String, userVerification: UserVerificationRequirement) {
        WAKLogger.d(TAG, "onExecute")
        val options = PublicKeyCredentialRequestOptions().apply {
            this.challenge = ByteArrayUtil.fromHex(challenge)
            this.rpId = relyingParty
            this.userVerification = userVerification
            if (credId.isNotEmpty()) {
                addAllowCredential(
                    credentialId = ByteArrayUtil.fromHex(credId),
                    transports = mutableListOf(AuthenticatorTransport.Internal)
                )
            }
        }

        try {
            val cred = webAuthnClient?.get(options)
            WAKLogger.d(TAG, "CHALLENGE:" + ByteArrayUtil.encodeBase64URL(options.challenge))
            cred?.let { showResultActivity(it) } ?: kotlin.run { showErrorPopup("cred null") }
        } catch (e: Exception) {
            WAKLogger.w(TAG, "failed to get")
            showErrorPopup(e.toString())
        }
    }


    private fun showErrorPopup(msg: String) {
        runOnUiThread {
            // Exemplo de toast para mostrar mensagens de erro
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun showResultActivity(cred: GetAssertionResponse) {
        WAKLogger.d(TAG, "show result activity")
        runOnUiThread {
            val intent = Intent(this, AuthenticationResultActivity::class.java).apply {
                putExtra("CRED_ID", cred.id)
                putExtra("CRED_RAW", ByteArrayUtil.toHex(cred.rawId))
                putExtra("CLIENT_JSON", cred.response.clientDataJSON)
                putExtra("AUTHENTICATOR_DATA", ByteArrayUtil.encodeBase64URL(cred.response.authenticatorData))
                putExtra("SIGNATURE", ByteArrayUtil.encodeBase64URL(cred.response.signature))
                putExtra("USER_HANDLE", ByteArrayUtil.encodeBase64URL(cred.response.userHandle!!))
            }
            WAKLogger.d(TAG, "start activity")
            startActivity(intent)
        }
    }
}
