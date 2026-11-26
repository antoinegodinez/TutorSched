package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.adapters.TutorAdapter
import com.example.tutorsched.models.Tutor
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewTutors: RecyclerView
    private lateinit var tutorAdapter: TutorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val btnViewMyBookings = findViewById<Button>(R.id.btnViewMyBookings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        recyclerViewTutors = findViewById(R.id.recyclerViewTutors)
        recyclerViewTutors.layoutManager = LinearLayoutManager(this)

        // Match this constructor to your Tutor data class properties
        val tutorList = listOf(
            Tutor("1", "Alex Smith", "Programming, CS101"),
            Tutor("2", "Jamie Reyes", "Database, SQL"),
            Tutor("3", "Chris Morales", "Data Structures")
        )

        tutorAdapter = TutorAdapter(this, tutorList)
        recyclerViewTutors.adapter = tutorAdapter

        btnViewMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}