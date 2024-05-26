package com.example.gauthdemo

// MainActivity.kt
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gauthdemo.ui.theme.GAuthDemoTheme
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes


class MainActivity : ComponentActivity() {

    private lateinit var signInClient: SignInClient
    private val REQUEST_AUTHORIZE = 1001

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential: SignInCredential? = Identity.getSignInClient(this).getSignInCredentialFromIntent(result.data)
            println("credential:")
            println(credential)
            val idToken = credential?.googleIdToken
            println("idToken:")
            println(idToken)
            sendTokenToBackend(idToken)
            requestCalendarAuthorization()
        } else {
            println("signInLauncher failed")
            // Handle sign-in failure
        }
    }

    private val authorizationLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val authorizationResult = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(result.data)
            // log authorizationResult:
            println(authorizationResult)

//            if (authorizationResult.isSuccessful) {
//                accessCalendarData()
//            } else {
//                // Handle authorization failure
//            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signInClient = Identity.getSignInClient(this)
        setContent {
            GAuthDemoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SignInScreen(
                        onSignInClick = { signIn() }
                    )
                }
            }
        }
    }

    private fun signIn() {
        println("signIn")
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.server_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                signInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
            }
            .addOnFailureListener { e ->
                // Handle error
                println("signIn failed")
                throw e
            }
    }

    private fun sendTokenToBackend(idToken: String?) {
        println("sendTokenToBackend")
        println(idToken)
        // Send the ID token to your backend
    }

    private fun requestCalendarAuthorization() {
        val requestedScopes = listOf(Scope(CalendarScopes.CALENDAR))
        val authorizationRequest = AuthorizationRequest.Builder().setRequestedScopes(requestedScopes).build()
        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    try {
                        authorizationLauncher.launch(IntentSenderRequest.Builder(authorizationResult.pendingIntent!!.intentSender).build())
                    } catch (e: IntentSender.SendIntentException) {
                        // Handle error
                        println("requestCalendarAuthorization failed1")
                        throw e
                    }
                } else {
                    // Access already granted
                    accessCalendarData()
                }
            }
            .addOnFailureListener { e ->
                // Handle error
                println("requestCalendarAuthorization failed2")
                throw e
            }
    }

    private fun accessCalendarData() {
        // Access Google Calendar data
        println("accessCalendarData")
        // Your code to access Google Calendar data
    }
}

@Composable
fun SignInScreen(onSignInClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Welcome to GAuthDemo", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        Button(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign In with Google")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GAuthDemoTheme {
        SignInScreen(onSignInClick = {})
    }
}