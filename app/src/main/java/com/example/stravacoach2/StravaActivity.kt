package com.example.stravacoach2

data class StravaActivity(
    val id: Long,
    val name: String,
    val distance: Float, // v metroch
    val moving_time: Int, // v sekundách
    val type: String,
    val start_date_local: String
)
