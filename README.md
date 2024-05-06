# WebAuthnKit (Android)

Esta biblioteca oferece uma maneira fácil de lidar com a API de Autenticação na Web do W3C (também conhecida como WebAuthN / FIDO 2.0).

## Atenção

ESTA VERSÃO AINDA NÃO É ESTÁVEL

Esta biblioteca não funciona como esperado no Android5 atualmente.

## Instalação


No build.gradle da sua aplicação:

```gradle
dependencies {
  implementation 'webauthnkit:webauthnkit-core:0.9.3'
}
```

pom

```xml
<dependency>
  <groupId>webauthnkit</groupId>
  <artifactId>webauthnkit-core</artifactId>
  <version>0.9.3</version>
  <type>pom</type>
</dependency>
```

## Começando

### Configuração do AutoBackup

Certifique-se de excluir 'webauthnkit.db'

- AndroidManifest.xml
```xml
<application
        android:fullBackupContent="@xml/backup_rules">
```

- values/backup_rules.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="database" path="webauthnkit.db" />
</full-backup-content>
```

Ou você pode simplesmente definir allowBackup="false".

```xml
<application
        android:allowBackup="false">
```

### Atividade

O WebAuthnKit usa recursos experimentais do Kotlin. Portanto, adicione algumas anotações à sua Activity.

`FragmentActivity` é necessário para ser vinculado aos recursos de UI do WebAuthnKit.
Claro, `androidx.appcompat.app.AppCompatActivity` também está bem.

```kotlin
@ExperimentalCoroutinesApi
@ExperimentalUnsignedTypes
class AuthenticationActivity : AppCompatActivity() {
  //...
}
```

### Configurar seu WebAuthnClient

Primeiro, prepare a UserConsentUI na sua Activity.

```kotlin
import webauthnkit.core.authenticator.internal.ui.UserConsentUI
import webauthnkit.core.authenticator.internal.ui.UserConsentUIFactory

var consentUI: UserConsentUI? = null

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  consentUI = UserConsentUIFactory.create(this)

  // Você pode configurar consent-ui aqui
  // consentUI.config.registrationDialogTitle = "Nova Chave de Login"
  // consentUI.config.selectionDialogTitle = "Selecione a Chave"
  // ...
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  consentUI?.onActivityResult(requestCode, resultCode, data)
}
```

Então, crie o WebAuthnClient

```kotlin
import webauthnkit.core.client.WebAuthnClient

val client = WebAuthnClient.create(
  activity = this,
  origin   = "https://example.org"
  ui       = consentUI!!
)
// Você pode configurar o cliente aqui
// client.maxTimeout = 120
// client.defaultTimeout = 60
```

### Fluxo de Registro

Com um fluxo que é descrito nos seguintes documentos, o WebAuthnClient cria uma credencial se for bem-sucedido.

- https://www.w3.org/TR/webauthn/#createCredential
- https://www.w3.org/TR/webauthn/#op-make-cred

```kotlin

private suspend fun executeRegistration() {

    val options = PublicKeyCredentialCreationOptions()

    options.challenge        = ByteArrayUtil.fromHex(challenge)
    options.user.id          = userId
    options.user.name        = username
    options.user.displayName = userDisplayName
    options.user.icon        = userIconURL
    options.rp.id            = "https://example.org"
    options.rp.name          = "nome_do_seu_serviço"
    options.rp.icon          = suaURLdeIconeDoServiço
    options.attestation      = attestationConveyance

    options.addPubKeyCredParam(
        alg = COSEAlgorithmIdentifier.es256
    )

    options.authenticatorSelection = AuthenticatorSelectionCriteria(
        requireResidentKey = true,
        userVerification   = userVerification
    )

    try {

        val credential = client.create(options)

        // enviar parâmetros para seu servidor
        // credential.id
        // credential.rawId
        // credential.response.attestationObject
        // credential.response.clientDataJSON

    } catch (e: Exception) {
        // tratamento de erro
    }

}

```

Se você quiser parar enquanto o cliente está em andamento, pode chamar o método cancel.

```kotlin
client.cancel()
```

`webauthnkit.core.CancelledException` será lançado na sua função suspendida.

### Fluxo de Autenticação

Com um fluxo que é descrito nos seguintes documentos, o WebAuthnClient encontra credenciais, permite que o usuário selecione uma (se múltiplas) e assina a resposta com ela.

- https://www.w3.org/TR/webauthn/#getAssertion
- https://www.w3.org/TR/webauthn/#op-get-assertion

```kotlin
private suspend fun executeAuthentication() {

    val options = PublicKeyCredentialRequestOptions()

    options.challenge        = ByteArrayUtil.fromHex(challenge)
    options.rpId             = relyingParty
    options.userVerification = userVerification

    if (credId.isNotEmpty()) {
        options.addAllowCredential(
            credentialId = ByteArrayUtil.fromHex(credId),
            transports   = mutableListOf(AuthenticatorTransport.Internal))
    }

    try {

        val assertion = client.get(options)

        // enviar parâmetros para seu servidor
        //assertion.id
        //assertion.rawId
        //assertion.response.authenticatorData
        //assertion.response.signature
        //assertion.response.userHandle
        //assertion.response.clientDataJSON

    } catch (e: Exception) {
        // tratamento de erro
    }

}
```

## Recursos

### Ainda não implementado

- Token Binding
- Extensões
- Autenticador BLE
- Serviço de Roaming BLE
- Attestation do SafetyNet

### Suporte de Algoritmo de Chave

- ES256

### Chave Residente

O InternalAuthenticator força o uso de chave residente.

### Attestation

Atualmente, esta biblioteca suporta apenas auto-attestation.

## Veja também

- https://www.w3.org/TR/webauthn/
- https://fidoalliance.org/specs/fido-v2.0-rd-20170927/fido-client-to-authenticator-protocol-v2.0-rd-20170927.html

## Licença

MIT-LICENSE

## Autor

Lyo K