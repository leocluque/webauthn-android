package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.luque.webauthn.authenticator.COSEAlgorithmIdentifier
import com.luque.webauthn.authenticator.internal.ui.UserConsentUI
import com.luque.webauthn.authenticator.internal.ui.UserConsentUIFactory
import com.luque.webauthn.client.WebAuthnClient
import com.luque.webauthn.data.AttestationConveyancePreference
import com.luque.webauthn.data.AuthenticatorSelectionCriteria
import com.luque.webauthn.data.MakeCredentialResponse
import com.luque.webauthn.data.PublicKeyCredentialCreationOptions
import com.luque.webauthn.data.UserVerificationRequirement
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.WAKLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.android.material.textfield.TextInputLayout

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class RegistrationActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegistrationActivity::class.simpleName
    }

    private lateinit var userIdField: TextInputLayout
    private lateinit var userNameField: TextInputLayout
    private lateinit var userDisplayNameField: TextInputLayout
    private lateinit var userIconURLField: TextInputLayout
    private lateinit var relyingPartyField: TextInputLayout
    private lateinit var relyingPartyIconField: TextInputLayout
    private lateinit var challengeField: TextInputLayout

    private val userVerificationOptions = listOf("Required", "Preferred", "Discouraged")
    private val attestationConveyanceOptions = listOf("Direct", "Indirect", "None")

    private var consentUI: UserConsentUI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        title = "Registration"

        // Initialize TextInputLayout fields
        userIdField = findViewById(R.id.userIdField)
        userNameField = findViewById(R.id.userNameField)
        userDisplayNameField = findViewById(R.id.userDisplayNameField)
        userIconURLField = findViewById(R.id.userIconURLField)
        relyingPartyField = findViewById(R.id.relyingPartyField)
        relyingPartyIconField = findViewById(R.id.relyingPartyIconField)
        challengeField = findViewById(R.id.challengeField)
    }

    private fun onStartClicked() {
        val userId = userIdField.editText?.text.toString()
        val username = userNameField.editText?.text.toString()
        val userDisplayName = userDisplayNameField.editText?.text.toString()
        val userIconURL = userIconURLField.editText?.text.toString()
        val relyingParty = relyingPartyField.editText?.text.toString()
        val relyingPartyICON = relyingPartyIconField.editText?.text.toString()
        val challenge = challengeField.editText?.text.toString()

        GlobalScope.launch {
            onExecute(
                userId = userId,
                username = username,
                userDisplayName = userDisplayName,
                userIconURL = userIconURL,
                relyingParty = relyingParty,
                relyingPartyICON = relyingPartyICON,
                challenge = challenge,
                userVerification = UserVerificationRequirement.Required,
                attestationConveyance = AttestationConveyancePreference.Direct
            )
        }
    }

    private fun createWebAuthnClient(): WebAuthnClient {
        consentUI = UserConsentUIFactory.create(this)
        return WebAuthnClient.create(
            activity = this,
            origin = "https://example.org",
            ui = consentUI!!
        ).apply {
            maxTimeout = 30
            defaultTimeout = 20
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        consentUI?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.registration_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.webauthn_registration_start -> {
                onStartClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun onExecute(
        userId: String,
        username: String,
        userDisplayName: String,
        userIconURL: String,
        relyingParty: String,
        relyingPartyICON: String,
        challenge: String,
        userVerification: UserVerificationRequirement,
        attestationConveyance: AttestationConveyancePreference
    ) {
        val options = PublicKeyCredentialCreationOptions().apply {
            this.challenge = ByteArrayUtil.fromHex(challenge)
            user.id = ByteArrayUtil.decodeBase64URL(userId)
            user.name = username
            user.displayName = userDisplayName
            user.icon = userIconURL
            rp.id = relyingParty
            rp.name = relyingParty
            rp.icon = relyingPartyICON
            attestation = attestationConveyance
            addPubKeyCredParam(alg = COSEAlgorithmIdentifier.es256)
            authenticatorSelection = AuthenticatorSelectionCriteria(
                requireResidentKey = true,
                userVerification = userVerification
            )
        }

        val webAuthnClient = createWebAuthnClient()

        try {
            val cred = webAuthnClient.create(options)
            WAKLogger.d(TAG, "CHALLENGE:" + ByteArrayUtil.encodeBase64URL(options.challenge))
            showResultActivity(cred)
        } catch (e: Exception) {
            WAKLogger.w(TAG, "failed to create")
            showErrorPopup(e.toString())
        }
    }

    private fun showErrorPopup(msg: String) {
        runOnUiThread {
            Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showResultActivity(cred: MakeCredentialResponse) {
        runOnUiThread {
            Intent(this, RegistrationResultActivity::class.java).apply {
                putExtra("CRED_ID", cred.id)
                putExtra("CRED_RAW", ByteArrayUtil.toHex(cred.rawId))
                putExtra("ATTESTATION", ByteArrayUtil.encodeBase64URL(cred.response.attestationObject))
                putExtra("CLIENT_JSON", cred.response.clientDataJSON)
                startActivity(this)
            }
        }
    }
}
