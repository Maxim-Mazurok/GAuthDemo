package com.example.gauthdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.calendar.CalendarScopes
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GAuthDemoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SignInScreen(onSignInClick = { signIn() })
                }
            }
        }

        // Configure sign-in to request the user's ID, basic profile, and email
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .requestServerAuthCode(getString(R.string.server_client_id))
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    private fun sendPostRequest(url: String, parameters: Map<String, String>) {
        val client = OkHttpClient()

        // Step 1: Create a JSON Object
        val json = JSONObject(parameters).toString()

        // Step 2: Convert JSON to RequestBody
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        // Step 3: Update Request Building
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Step 4: Send Request
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                // Handle failure
                Log.e("MainActivity", "onFailure Error: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    // Handle success
                    Log.d("MainActivity", "onResponse Response: $responseBody")
                } else {
                    // Handle error
                    Log.e("MainActivity", "onResponse Error: ${response.code}, $responseBody")
                }
            }
        })
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val authCode = account?.serverAuthCode

            // Log the tokens
            Log.d("MainActivity", "ID Token: $idToken")
            Log.d("MainActivity", "Auth Code: $authCode")

            // TODO: replace the URL with actual server URL and remove plain text policy
            sendPostRequest("http://172.20.154.244:3000/auth", mapOf("idToken" to idToken!!, "authCode" to authCode!!))

            // Update UI or perform other actions with the account information
            updateUI(account)
        } catch (e: ApiException) {
            Log.w("MainActivity", "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            // User is signed in
            // Update your UI here with account information
        } else {
            // User is signed out
            // Update your UI here
        }
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
