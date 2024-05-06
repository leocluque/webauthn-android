package com.luque.webauthn_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class BleAuthenticatorAuthenticationActivity : AppCompatActivity() {

    companion object {
        val TAG = BleAuthenticatorAuthenticationActivity::class.simpleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "AUTHENTICATION BLE SERVICE"

//        verticalLayout {
//
//            padding = dip(10)
//
//            button("START") {
//                textSize = 24f
//
//                onClick {
//
//                }
//
//            }
//
//            button("CLOSE") {
//                textSize = 24f
//
//                onClick {
//
//                }
//            }
//
//        }
    }

}
