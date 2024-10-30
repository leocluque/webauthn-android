package com.luque.webauthn.authenticator.internal.ui

import androidx.fragment.app.FragmentActivity
import com.luque.webauthn.util.WAKLogger

object UserConsentUIFactory {
    val TAG = UserConsentUIFactory::class.simpleName
    fun create(activity: FragmentActivity): UserConsentUI {
        WAKLogger.d(TAG, "create")
        return DefaultUserConsentUI(activity)
    }
}

