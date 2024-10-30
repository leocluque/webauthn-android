package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.luque.webauthn.authenticator.COSEAlgorithmIdentifier
import com.luque.webauthn.authenticator.internal.ui.UserConsentUI
import com.luque.webauthn.authenticator.internal.ui.UserConsentUIFactory
import com.luque.webauthn.client.WebAuthClient
import com.luque.webauthn.data.AttestationConveyancePreference
import com.luque.webauthn.data.AuthenticatorSelectionCriteria
import com.luque.webauthn.data.MakeCredentialResponse
import com.luque.webauthn.data.PublicKeyCredentialCreationOptions
import com.luque.webauthn.data.UserVerificationRequirement
import com.luque.webauthn.util.ByteArrayUtil
import com.luque.webauthn.util.WAKLogger
import kotlinx.coroutines.launch



class RegistrationActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegistrationActivity::class.simpleName
    }

    private lateinit var userIdField: EditText
    private lateinit var userNameField: EditText
    private lateinit var userDisplayNameField: EditText
    private lateinit var userIconURLField: EditText
    private lateinit var relyingPartyField: EditText
    private lateinit var relyingPartyIconField: EditText
    private lateinit var challengeField: EditText
    private lateinit var userVerificationSpinner: Spinner
    private lateinit var attestationConveyanceSpinner: Spinner
    private var consentUI: UserConsentUI? = null

    private val userVerificationOptions = listOf("Required", "Preferred", "Discouraged")
    private val attestationConveyanceOptions = listOf("Direct", "Indirect", "None")

    private val webAuthnClient by lazy {
        createWebAuthnClient()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Inicializa as views usando View Binding, se disponível
        userIdField = findViewById(R.id.userIdField)
        userNameField = findViewById(R.id.userNameField)
        userDisplayNameField = findViewById(R.id.userDisplayNameField)
        userIconURLField = findViewById(R.id.userIconURLField)
        relyingPartyField = findViewById(R.id.relyingPartyField)
        relyingPartyIconField = findViewById(R.id.relyingPartyIconField)
        challengeField = findViewById(R.id.challengeField)
        userVerificationSpinner = findViewById(R.id.userVerificationSpinner)
        attestationConveyanceSpinner = findViewById(R.id.attestationConveyanceSpinner)

        val userVerificationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userVerificationOptions)
        userVerificationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userVerificationSpinner.adapter = userVerificationAdapter

        val attestationConveyanceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, attestationConveyanceOptions)
        attestationConveyanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        attestationConveyanceSpinner.adapter = attestationConveyanceAdapter

        userIconURLField.setText("https://www.gravatar.com/avatar/0b63462eb18efbfb764b0c226abff4a0?s=440&d=retro")
    }

    private fun onStartClicked() {
        val userId = userIdField.text.toString()
        val username = userNameField.text.toString()
        val userDisplayName = userDisplayNameField.text.toString()
        val userIconURL = userIconURLField.text.toString()
        val relyingParty = relyingPartyField.text.toString()
        val relyingPartyICON = relyingPartyIconField.text.toString()
        val challenge = challengeField.text.toString()

        lifecycleScope.launch {
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

    private fun createWebAuthnClient(): WebAuthClient {
        consentUI = UserConsentUIFactory.create(this)
        return WebAuthClient.create(
            activity = this,
            origin = "https://example.org",
            ui = consentUI!!,
            coroutineScope = this.lifecycleScope
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

        try {
            val cred = webAuthnClient.create(options, this.lifecycleScope)
            WAKLogger.d(TAG, "CHALLENGE:" + ByteArrayUtil.encodeBase64URL(options.challenge))
            showResultActivity(cred)
        } catch (e: Exception) {
            WAKLogger.w(TAG, "failed to create")
            showErrorPopup(e.toString())
        }
    }

    private fun showErrorPopup(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun showResultActivity(cred: MakeCredentialResponse) {
        Intent(this, RegistrationResultActivity::class.java).apply {
            putExtra("CRED_ID", cred.id)
            putExtra("CRED_RAW", ByteArrayUtil.toHex(cred.rawId))
            putExtra("ATTESTATION", ByteArrayUtil.encodeBase64URL(cred.response.attestationObject))
            putExtra("CLIENT_JSON", cred.response.clientDataJSON)
            startActivity(this)
        }
    }
}
