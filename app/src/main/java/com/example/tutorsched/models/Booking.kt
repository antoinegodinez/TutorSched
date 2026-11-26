package com.example.tutorsched.models

data class Booking(
    val tutorName: String,
    val subject: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    var status: String,
    val documentId: String = ""
)