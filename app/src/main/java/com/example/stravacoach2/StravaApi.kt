package com.example.stravacoach2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StravaApi {
    @GET("athlete/activities")
    fun getActivities(
        @Header("Authorization") auth: String,
        @Query("per_page") perPage: Int = 10
    ): Call<List<StravaActivity>>
}
