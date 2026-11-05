package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tutorAdapter: TutorAdapter
    private lateinit var tutorList: List<Tutor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTutors)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Find the "View My Bookings" button and set its click listener
        val btnMyBookings = findViewById<Button>(R.id.btnViewMyBookings)
        btnMyBookings.setOnClickListener {
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }

        // Sample mock data for tutors
        tutorList = listOf(
            Tutor("Alex Smith", "Programming, CS101", "Available"),
            Tutor("Jamie Reyes", "Database, SQL", "Available"),
            Tutor("Chris Morales", "Data Structures", "Available")
        )

        tutorAdapter = TutorAdapter(this, tutorList)
        recyclerView.adapter = tutorAdapter
    }
}