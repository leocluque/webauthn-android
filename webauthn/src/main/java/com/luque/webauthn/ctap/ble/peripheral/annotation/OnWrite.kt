package com.luque.webauthn.ctap.ble.peripheral.annotation


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class OnWrite(val uuid: String)