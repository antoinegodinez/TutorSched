package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TutorScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private val requestList = mutableListOf<Booking>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_schedule)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewTutorSchedule)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(this, requestList)
        recyclerView.adapter = bookingAdapter

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        fetchTutorBookings()
    }

    private fun fetchTutorBookings() {
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                requestList.clear()
                for (document in result) {
                    val tutorName = document.getString("tutorName") ?: "Tutor"
                    val subject = document.getString("subject") ?: "General"
                    val date = document.getString("sessionDate") ?: ""
                    val time = document.getString("timeSlot") ?: ""
                    val status = document.getString("status") ?: "Pending"

                    val booking = Booking(
                        tutorName = tutorName,
                        subject = subject,
                        date = date,
                        startTime = time,
                        endTime = "",
                        status = status
                    )
                    requestList.add(booking)
                }
                bookingAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load tutor schedule: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}