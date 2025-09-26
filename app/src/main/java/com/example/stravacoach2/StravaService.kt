package com.example.stravacoach2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StravaService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.strava.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: StravaApi = retrofit.create(StravaApi::class.java)
}
