package com.example.stravacoach2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class AuthCallbackActivity : AppCompatActivity() {

    private val CLIENT_ID by lazy { getString(R.string.strava_client_id) }
    private val CLIENT_SECRET by lazy { getString(R.string.strava_client_secret) }
    private val REDIRECT_URI by lazy { getString(R.string.strava_redirect_uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleStravaRedirect(intent)
    }

    private fun handleStravaRedirect(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")

            when {
                error != null -> {
                    Log.e("AuthCallback", "Auth error: $error")
                    finish()
                }
                code != null -> {
                    Log.d("AuthCallback", "Authorization code: $code")
                    exchangeToken(code)
                }
            }
        }
    }

    private fun exchangeToken(code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url = "https://www.strava.com/oauth/token" +
                        "?client_id=$CLIENT_ID" +
                        "&client_secret=$CLIENT_SECRET" +
                        "&code=$code" +
                        "&grant_type=authorization_code"

                val request = Request.Builder()
                    .url(url)
                    .post(FormBody.Builder().build())
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("AuthCallback", "Token exchange failed: ${response.code}")
                        finish()
                        return@use
                    }

                    val json = JSONObject(response.body!!.string())
                    val accessToken = json.getString("access_token")
                    Log.d("AuthCallback", "Access token: $accessToken")

                    // Posielame token späť do MainActivity
                    val intent = Intent(this@AuthCallbackActivity, MainActivity::class.java)
                    intent.putExtra("access_token", accessToken)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)

                    finish() // zavrie AuthCallbackActivity
                }
            } catch (e: Exception) {
                Log.e("AuthCallback", "Token exchange failed", e)
                finish()
            }
        }
    }
}
