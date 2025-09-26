package com.example.stravacoach2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private var accessToken: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityAdapter
    private val activitiesList = mutableListOf<StravaActivity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ActivityAdapter(activitiesList)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.buttonStravaAuth).setOnClickListener {
            openStravaAuth()
        }

        // Skontrolujeme, či sme prišli s tokenom z AuthCallbackActivity
        intent.getStringExtra("access_token")?.let { token ->
            accessToken = token
            Log.d("MainActivity", "Access token prijatý: $accessToken")
            fetchActivities()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("access_token")?.let { token ->
            accessToken = token
            Log.d("MainActivity", "Access token prijatý: $accessToken")
            fetchActivities()
        }
    }

    private fun openStravaAuth() {
        val CLIENT_ID = getString(R.string.strava_client_id)
        val REDIRECT_URI = getString(R.string.strava_redirect_uri)
        val authUrl = Uri.Builder()
            .scheme("https")
            .authority("www.strava.com")
            .appendPath("oauth")
            .appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "read,activity:read_all")
            .build()

        Log.d("MainActivity", "Opening auth URL: $authUrl")
        startActivity(Intent(Intent.ACTION_VIEW, authUrl))
    }

    private fun fetchActivities() {
        if (accessToken == null) {
            Log.d("MainActivity", "Access Token Null")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.strava.com/api/v3/athlete/activities?per_page=10")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("MainActivity", "Fetch activities failed: ${response.code}")
                        return@use
                    }

                    val responseBody = response.body!!.string()
                    val jsonArray = JSONArray(responseBody)
                    activitiesList.clear()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        activitiesList.add(
                            StravaActivity(
                                id = obj.getLong("id"),
                                name = obj.getString("name"),
                                distance = obj.getDouble("distance").toFloat(),
                                moving_time = obj.getInt("moving_time"),
                                type = obj.getString("type"),
                                start_date_local = obj.getString("start_date_local")
                            )
                        )
                    }

                    Log.d("MainActivity", "Loaded activities: ${activitiesList.size}")
                    runOnUiThread { adapter.notifyDataSetChanged() }
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Fetch activities failed", e)
            }
        }
    }
}
