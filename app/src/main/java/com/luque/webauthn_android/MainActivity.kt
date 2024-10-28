package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.luque.webauthn.util.WAKLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi



class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.simpleName
    }

    private val REQUEST_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Define o layout XML

        WAKLogger.available = true

        // Inicialize os botões
        val registrationButton: Button = findViewById(R.id.registration_button)
        val authenticationButton: Button = findViewById(R.id.authentication_button)

        // Configura o clique do botão Registration
        registrationButton.setOnClickListener {
            goToRegistrationActivity()
        }

        // Configura o clique do botão Authentication
        authenticationButton.setOnClickListener {
            goToAuthenticationActivity()
        }

        /*
        // Configura o clique do botão Registration (BLE)
        val registrationBleButton: Button = findViewById(R.id.registration_ble_button)
        registrationBleButton.setOnClickListener {
            goToBleRegistrationActivity()
        }

        // Configura o clique do botão Authentication (BLE)
        val authenticationBleButton: Button = findViewById(R.id.authentication_ble_button)
        authenticationBleButton.setOnClickListener {
            goToBleAuthenticationActivity()
        }
        */
    }

    private fun goToRegistrationActivity() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun goToAuthenticationActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
    }

    /*
    private fun checkPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) {
            WAKLogger.d(TAG, "not granted!")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS)
            return false
        } else {
            WAKLogger.d(TAG, "granted!")
            return true
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun goToBleRegistrationActivity() {
        if (checkPermission()) {
            val intent = Intent(this, BleAuthenticatorRegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun goToBleAuthenticationActivity() {
        if (checkPermission()) {
            val intent = Intent(this, BleAuthenticatorAuthenticationActivity::class.java)
            startActivity(intent)
        }
    }

    @OnShowRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForLocation(request: PermissionRequest) {
        showRationaleDialog("", request)
    }

    @OnPermissionDenied(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        Snackbar.make(this.contentView!!,
            "location permission needed to use bluetooth",
            Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun showRationaleDialog(message: String, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton("") { _, _ -> request.proceed() }
            .setNegativeButton("") { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(message)
            .show()
    }
    */
}
