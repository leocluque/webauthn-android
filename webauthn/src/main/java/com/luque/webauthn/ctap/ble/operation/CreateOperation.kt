package com.luque.webauthn.ctap.ble.operation

import java.util.*

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import com.luque.webauthn.error.ErrorReason
import com.luque.webauthn.authenticator.AttestationObject
import com.luque.webauthn.authenticator.MakeCredentialSession
import com.luque.webauthn.authenticator.MakeCredentialSessionListener
import com.luque.webauthn.client.operation.OperationListener
import com.luque.webauthn.client.operation.OperationType
import com.luque.webauthn.ctap.options.MakeCredentialOptions
import com.luque.webauthn.ctap.response.MakeCredentialResponseBuilder
import com.luque.webauthn.data.AuthenticatorTransport
import com.luque.webauthn.data.PublicKeyCredentialDescriptor
import com.luque.webauthn.error.BadOperationException
import com.luque.webauthn.util.WAKLogger


@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class CreateOperation(
    private val options: MakeCredentialOptions,
    private val session: MakeCredentialSession,
    private val lifetimeTimer: Long
) {

    companion object {
        val TAG = CreateOperation::class.simpleName
    }

    val opId: String = UUID.randomUUID().toString()
    var listener: OperationListener? = null

    private var stopped: Boolean = false

    private val sessionListener = object : MakeCredentialSessionListener {

        override fun onAvailable(session: MakeCredentialSession) {
            WAKLogger.d(TAG, "onAvailable")

            if (stopped) {
                WAKLogger.d(TAG, "already stopped")
                return
            }

            if (options.requireResidentKey && !session.canStoreResidentKey()) {
                WAKLogger.d(TAG, "This authenticator can't store resident-key")
                stop(ErrorReason.Unsupported)
                return
            }

            if (options.requireUserVerification && !session.canPerformUserVerification()) {
                WAKLogger.d(TAG, "This authenticator can't perform user verification")
                stop(ErrorReason.Unsupported)
                return
            }

            val requireUserPresence = !options.requireUserVerification

            // TODO currently not supported
            val excludeCredentialDescriptorList =
                arrayListOf<PublicKeyCredentialDescriptor>()

            session.makeCredential(
                hash                            = options.clientDataHash,
                rpEntity                        = options.rp,
                userEntity                      = options.user,
                requireResidentKey              = options.requireResidentKey,
                requireUserPresence             = requireUserPresence,
                requireUserVerification         = options.requireUserVerification,
                credTypesAndPubKeyAlgs          = options.pubKeyCredParams,
                excludeCredentialDescriptorList = excludeCredentialDescriptorList
            )
        }

        override fun onCredentialCreated(session: MakeCredentialSession, attestationObject: AttestationObject) {
            WAKLogger.d(TAG, "onCredentialCreated")

            val (result, error) =
                MakeCredentialResponseBuilder(attestationObject).build()

            if (error != null) {
                stop(error)
                return
            }

            completed()

            continuation?.resume(result!!)
            continuation = null

        }

        override fun onOperationStopped(session: MakeCredentialSession, reason: ErrorReason) {
            WAKLogger.d(TAG, "onOperationStopped")
            stop(reason)
        }

        override fun onUnavailable(session: MakeCredentialSession) {
            WAKLogger.d(TAG, "onUnavailable")
            stop(ErrorReason.NotAllowed)
        }

    }

    private var continuation: Continuation<ByteArray>? = null

    suspend fun start(): ByteArray = suspendCoroutine { cont ->

        WAKLogger.d(TAG, "start")

        GlobalScope.launch {

            if (stopped) {
                WAKLogger.d(TAG, "already stopped")
                cont.resumeWithException(BadOperationException())
                listener?.onFinish(OperationType.Create, opId)
                return@launch
            }

            if (continuation != null) {
                WAKLogger.d(TAG, "continuation already exists")
                cont.resumeWithException(BadOperationException())
                listener?.onFinish(OperationType.Create, opId)
                return@launch
            }

            continuation = cont

            startTimer()

            session.listener = sessionListener
            session.start()
        }
    }

    fun cancel(reason: ErrorReason = ErrorReason.Timeout) {
        WAKLogger.d(TAG, "cancel")
        if (continuation != null && !this.stopped) {
            GlobalScope.launch {
                when (session.transport) {
                    AuthenticatorTransport.Internal -> {
                        when (reason) {
                            ErrorReason.Timeout -> {
                                session.cancel(ErrorReason.Timeout)
                            }
                            else -> {
                                session.cancel(ErrorReason.Cancelled)
                            }
                        }
                    }
                    else -> {
                        stop(reason)
                    }
                }
            }
        }
    }

    private fun stop(reason: ErrorReason) {
        WAKLogger.d(TAG, "stop")
        stopInternal(reason)
        dispatchError(reason)
    }

    private fun completed() {
        WAKLogger.d(TAG, "completed")
        stopTimer()
        listener?.onFinish(OperationType.Create, opId)
    }

    private fun stopInternal(reason: ErrorReason) {
        WAKLogger.d(TAG, "stopInternal")
        if (continuation == null) {
            WAKLogger.d(TAG, "not started")
            // not started
            return
        }
        if (stopped) {
            WAKLogger.d(TAG, "already stopped")
            return
        }
        stopTimer()
        session.cancel(reason)
        listener?.onFinish(OperationType.Create, opId)
    }

    private fun dispatchError(reason: ErrorReason) {
        WAKLogger.d(TAG, "dispatchError")
        continuation?.resumeWithException(reason.rawValue)
    }

    private var timer: Timer? = null

    private fun startTimer() {
        WAKLogger.d(TAG, "startTimer")
        stopTimer()
        timer = Timer()
        timer!!.schedule(object: TimerTask(){
            override fun run() {
                timer = null
                onTimeout()
            }
        }, lifetimeTimer*1000)
    }

    private fun stopTimer() {
        WAKLogger.d(TAG, "stopTimer")
        timer?.cancel()
        timer = null
    }

    private fun onTimeout() {
        WAKLogger.d(TAG, "onTimeout")
        stopTimer()
        cancel(ErrorReason.Timeout)
    }
}
