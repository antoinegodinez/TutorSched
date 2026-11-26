package com.example.tutorsched.models

data class Tutor(
    val name: String,
    val subjects: String,
    val status: String = "Available",
    val id: String = ""
)