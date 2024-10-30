package com.luque.webauthn.client

import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luque.webauthn.authenticator.Authenticator
import com.luque.webauthn.authenticator.internal.InternalAuthenticator
import com.luque.webauthn.authenticator.internal.ui.UserConsentUI
import com.luque.webauthn.client.operation.CreateOperation
import com.luque.webauthn.client.operation.GetOperation
import com.luque.webauthn.client.operation.OperationListener
import com.luque.webauthn.client.operation.OperationType
import com.luque.webauthn.data.CollectedClientData
import com.luque.webauthn.data.CollectedClientDataType
import com.luque.webauthn.data.GetAssertionResponse
import com.luque.webauthn.data.MakeCredentialResponse
import com.luque.webauthn.data.PublicKeyCredentialCreationOptions
import com.luque.webauthn.data.PublicKeyCredentialRequestOptions
import com.luque.webauthn.util.ByteArrayUtil
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap


class WebAuthClient(
    val authenticator: Authenticator,
    private val origin: String,
) : OperationListener {

    companion object {
        val TAG = WebAuthClient::class.simpleName
        private val gson: Gson = GsonBuilder()
            .serializeNulls() // Opcional: Inclui campos nulos, remova se quiser ignorá-los.
            .create()

        fun create(
            activity: FragmentActivity,
            origin: String,
            ui: UserConsentUI,
            coroutineScope: CoroutineScope,
        ): WebAuthClient {
            val authenticator =
                InternalAuthenticator(activity = activity, ui = ui, coroutineScope = coroutineScope)
            return WebAuthClient(origin = origin, authenticator = authenticator)
        }
    }

    var defaultTimeout: Long = 60
    var minTimeout: Long = 15
    var maxTimeout: Long = 120

    private val getOperations = ConcurrentHashMap<String, GetOperation>()
    private val createOperations = ConcurrentHashMap<String, CreateOperation>()

    suspend fun get(options: PublicKeyCredentialRequestOptions, coroutineScope: CoroutineScope): GetAssertionResponse {
        val op = newGetOperation(options)
        op.listener = this
        getOperations[op.opId] = op
        return op.start(coroutineScope)
    }

    private fun newGetOperation(options: PublicKeyCredentialRequestOptions): GetOperation {
        val timer = adjustLifetimeTimer(options.timeout)
        val rpId = options.rpId ?: origin

        val (data, json, hash) = generateClientData(
            type = CollectedClientDataType.Get,
            challenge = ByteArrayUtil.encodeBase64URL(options.challenge)
        )

        val session = authenticator.newGetAssertionSession()

        return GetOperation(
            options = options,
            rpId = rpId,
            session = session,
            clientData = data,
            clientDataJSON = json,
            clientDataHash = hash,
            lifetimeTimer = timer
        )
    }

    suspend fun create(options: PublicKeyCredentialCreationOptions, coroutineScope: CoroutineScope): MakeCredentialResponse {
        val op = newCreateOperation(options)
        op.listener = this
        createOperations[op.opId] = op
        return op.start(coroutineScope)
    }

    private fun newCreateOperation(options: PublicKeyCredentialCreationOptions): CreateOperation {
        val timer = adjustLifetimeTimer(options.timeout)
        val rpId = options.rp.id ?: origin

        val (data, json, hash) = generateClientData(
            type = CollectedClientDataType.Create,
            challenge = ByteArrayUtil.encodeBase64URL(options.challenge)
        )

        val session = authenticator.newMakeCredentialSession()

        return CreateOperation(
            options = options,
            rpId = rpId,
            session = session,
            clientData = data,
            clientDataJSON = json,
            clientDataHash = hash,
            lifetimeTimer = timer
        )
    }

    fun cancel(coroutineScope: CoroutineScope) {
        getOperations.values.forEach { it.cancel(coroutineScope = coroutineScope) }
        createOperations.values.forEach { it.cancel(coroutineScope = coroutineScope) }
        getOperations.clear()
        createOperations.clear()
    }

    private fun adjustLifetimeTimer(timeout: Long?): Long {
        return timeout?.coerceIn(minTimeout, maxTimeout) ?: defaultTimeout
    }

    private fun generateClientData(
        type: CollectedClientDataType,
        challenge: String,
    ): Triple<CollectedClientData, String, ByteArray> {
        val data =
            CollectedClientData(type = type.toString(), challenge = challenge, origin = origin)
        val json = encodeJSON(data)
        val hash = ByteArrayUtil.sha256(json)
        return Triple(data, json, hash)
    }

    private fun encodeJSON(data: CollectedClientData): String {
        return gson.toJson(data)
    }

    override fun onFinish(opType: OperationType, opId: String) {
        when (opType) {
            OperationType.Get -> getOperations.remove(opId)
            OperationType.Create -> createOperations.remove(opId)
        }
    }
}

