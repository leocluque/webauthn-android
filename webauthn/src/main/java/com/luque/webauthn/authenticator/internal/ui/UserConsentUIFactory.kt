package com.luque.webauthn.authenticator.internal.ui

import android.os.Build
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.luque.webauthn.util.WAKLogger



object UserConsentUIFactory {
    val TAG = UserConsentUIFactory::class.simpleName
    fun create(activity: FragmentActivity): UserConsentUI {
        WAKLogger.d(TAG, "create")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DefaultUserConsentUI(activity)
        } else {
            LegacyUserConsentUI(activity)
        }
    }
}

