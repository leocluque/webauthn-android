package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
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

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class RegistrationActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegistrationActivity::class.simpleName
    }

    var userIdField: EditText? = null
    var userNameField: EditText? = null
    var userDisplayNameField: EditText? = null
    var userIconURLField: EditText? = null
    var relyingPartyField: EditText? = null
    var relyingPartyIconField: EditText? = null
    var challengeField: EditText? = null

//    var userVerificationSpinner:      MaterialSpinner? = null
//    var attestationConveyanceSpinner: MaterialSpinner? = null

    val userVerificationOptions = listOf("Required", "Preferred", "Discouraged")
    val attestationConveyanceOptions = listOf("Direct", "Indirect", "None")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        title = "REGISTRATION"

//        verticalLayout {
//
//            padding = dip(10)
//
//            textView {
//                text = "User Id(Base64)"
//            }
//
//            userIdField = editText {
//                singleLine = true
//            }
//            userIdField!!.setText("lyokato")
//
//            textView {
//                text = "User Name"
//            }
//
//            userNameField = editText {
//                singleLine = true
//            }
//            userNameField!!.setText("lyokato")
//
//            textView {
//                text = "User Display Name"
//            }
//
//            userDisplayNameField = editText {
//                singleLine = true
//            }
//            userDisplayNameField!!.setText("Lyo Kato")
//
//            textView {
//                text = "User ICON URL (Optional)"
//            }
//
//            userIconURLField = editText {
//                singleLine = true
//            }
//            userIconURLField!!.setText("https://www.gravatar.com/avatar/0b63462eb18efbfb764b0c226abff4a0?s=440&d=retro")
//
//            textView {
//                text = "Relying Party"
//            }
//
//            relyingPartyField = editText {
//                singleLine = true
//            }
//            relyingPartyField!!.setText("https://example.org")
//
//            textView {
//                text = "Relying Party ICON"
//            }
//
//            relyingPartyIconField = editText {
//                singleLine = true
//            }
//            relyingPartyIconField!!.setText("https://developers.google.com/identity/images/g-logo.png")
//
//            textView {
//                text = "Challenge (Hex)"
//            }
//
//            challengeField = editText {
//                singleLine = true
//            }
//            challengeField!!.setText("E54F333A94D24AA4A1AAC181B389FBCCEC2874BDED40E17E527ACD79CBE42E2C")
//
//            val spinnerWidth = 160
//
//            relativeLayout {
//
//                lparams {
//                    width = matchParent
//                    height = wrapContent
//                    margin = dip(10)
//                }
//
//                backgroundColor = Color.parseColor("#eeeeee")
//
//                textView {
//                    padding = dip(10)
//                    text = "UV"
//
//                }.lparams {
//                    width = wrapContent
//                    height = wrapContent
//                    margin = dip(10)
//                    alignParentLeft()
//                    centerVertically()
//                }
//
//                userVerificationSpinner = materialSpinner {
//
//                    padding = dip(10)
//
//                    lparams {
//                        width = dip(spinnerWidth)
//                        height = wrapContent
//                        margin = dip(10)
//                        alignParentRight()
//                        centerVertically()
//                    }
//                }
//
//                userVerificationSpinner!!.setItems(userVerificationOptions)
//            }
//
//            relativeLayout {
//
//                lparams {
//                    width = matchParent
//                    height = wrapContent
//                    margin = dip(10)
//                }
//
//                backgroundColor = Color.parseColor("#eeeeee")
//
//                textView {
//                    padding = dip(10)
//                    text = "Attestation"
//
//                }.lparams {
//                    width = wrapContent
//                    height = wrapContent
//                    margin = dip(10)
//                    alignParentLeft()
//                    centerVertically()
//                }
//
//                attestationConveyanceSpinner = materialSpinner {
//
//                    padding = dip(10)
//
//                    lparams {
//                        width = dip(spinnerWidth)
//                        height = wrapContent
//                        margin = dip(10)
//                        alignParentRight()
//                        centerVertically()
//                    }
//                }
//
//                attestationConveyanceSpinner!!.setItems(attestationConveyanceOptions)
//            }
//
//        }

    }

    private fun onStartClicked() {

        // TODO validation
        val userId = userIdField!!.text.toString()
        val username = userNameField!!.text.toString()
        val userDisplayName = userDisplayNameField!!.text.toString()
        val userIconURL = userIconURLField!!.text.toString()
        val relyingParty = relyingPartyField!!.text.toString()
        val relyingPartyICON = relyingPartyIconField!!.text.toString()
        val challenge = challengeField!!.text.toString()

//        val userVerification  =
//            when (userVerificationOptions[userVerificationSpinner!!.selectedIndex]) {
//                "Required"    -> { UserVerificationRequirement.Required    }
//                "Preferred"   -> { UserVerificationRequirement.Preferred   }
//                "Discouraged" -> { UserVerificationRequirement.Discouraged }
//                else          -> { UserVerificationRequirement.Preferred   }
//            }
//        val attestationConveyance =
//            when (attestationConveyanceOptions[attestationConveyanceSpinner!!.selectedIndex]) {
//                "Direct"   -> { AttestationConveyancePreference.Direct   }
//                "Indirect" -> { AttestationConveyancePreference.Indirect }
//                "None"     -> { AttestationConveyancePreference.None     }
//                else       -> { AttestationConveyancePreference.Direct   }
//            }

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

        val webAuthnClient = WebAuthnClient.create(
            activity = this,
            origin = "https://example.org",
            ui = consentUI!!
        )

        webAuthnClient.maxTimeout = 30
        webAuthnClient.defaultTimeout = 20

        return webAuthnClient
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        consentUI?.onActivityResult(requestCode, resultCode, data)
        /*
        if (consentUI != null && consentUI!!.onActivityResult(requestCode, resultCode, data)) {
            return
        }
        */
    }

    override fun onStart() {
        WAKLogger.d(TAG, "onStart")
        super.onStart()
    }

    override fun onStop() {
        WAKLogger.d(TAG, "onStop")
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.registration_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.webauthn_registration_start) {
            onStartClicked()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    var consentUI: UserConsentUI? = null

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

        val options = PublicKeyCredentialCreationOptions()

        options.challenge = ByteArrayUtil.fromHex(challenge)
        options.user.id = ByteArrayUtil.decodeBase64URL(userId)
        options.user.name = username
        options.user.displayName = userDisplayName
        options.user.icon = userIconURL
        options.rp.id = relyingParty
        options.rp.name = relyingParty
        options.rp.icon = relyingPartyICON
        options.attestation = attestationConveyance

        options.addPubKeyCredParam(
            alg = COSEAlgorithmIdentifier.es256
        )

        options.authenticatorSelection = AuthenticatorSelectionCriteria(
            requireResidentKey = true,
            userVerification = userVerification
        )

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
//        runOnUiThread {
//            toast(msg)
//        }
    }

    private fun showResultActivity(cred: MakeCredentialResponse) {
        runOnUiThread {
            val intent = Intent(this, RegistrationResultActivity::class.java)
            intent.putExtra("CRED_ID", cred.id)
            intent.putExtra("CRED_RAW", ByteArrayUtil.toHex(cred.rawId))

            intent.putExtra(
                "ATTESTATION",
                ByteArrayUtil.encodeBase64URL(cred.response.attestationObject)
            )
            intent.putExtra("CLIENT_JSON", cred.response.clientDataJSON)
            startActivity(intent)
        }
    }

}
