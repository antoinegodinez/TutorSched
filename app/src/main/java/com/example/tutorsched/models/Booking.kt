package com.example.tutorsched

// Structure for a tutorial booking request
data class Booking(
    val tutorName: String,
    val subject: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    var status: String
)