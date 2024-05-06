package com.luque.webauthn_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luque.webauthn.util.WAKLogger
import com.luque.webauthn_android.AuthenticationActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi


/* currently, comment out BLE related code
import android.Manifest
import permissions.dispatcher.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import android.content.pm.PackageManager
*/

// @RuntimePermissions
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.simpleName
    }

    private val REQUEST_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WAKLogger.available = true

//        verticalLayout {
//
//            padding = dip(10)
//
//            button("Registration") {
//                textSize = 24f
//
//                onClick {
//                    goToRegistrationActivity()
//                }
//
//            }
//
//            button("Authentication") {
//                textSize = 24f
//
//                onClick {
//                    goToAuthenticationActivity()
//                }
//            }

            /*
            button("Registration (BLE)") {
                textSize = 24f

                onClick {
                    goToBleRegistrationActivity()
                }

            }

            button("Authentication (BLE)") {
                textSize = 24f

                onClick {
                    goToBleAuthenticationActivity()
                }
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
