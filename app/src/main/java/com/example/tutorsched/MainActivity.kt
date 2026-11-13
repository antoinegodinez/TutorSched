package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewTutors: RecyclerView
    private lateinit var tutorAdapter: TutorAdapter
    private val tutorList = mutableListOf<Tutor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val btnMyBookings = findViewById<Button>(R.id.btnMyBookings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        recyclerViewTutors = findViewById(R.id.recyclerViewTutors)

        recyclerViewTutors.layoutManager = LinearLayoutManager(this)

        // Populate sample available tutors
        tutorList.add(Tutor("Alex Smith", "Programming, CS101", "Available"))
        tutorList.add(Tutor("Jamie Johnson", "Database Systems", "Available"))
        tutorList.add(Tutor("Sam Wilson", "Data Structures & Algorithms", "Available"))

        tutorAdapter = TutorAdapter(this, tutorList)
        recyclerViewTutors.adapter = tutorAdapter

        // Navigate to My Bookings screen
        btnMyBookings?.setOnClickListener {
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }

        // Secure Logout functionality
        btnLogout?.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}