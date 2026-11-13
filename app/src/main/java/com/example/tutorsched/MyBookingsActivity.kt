package com.example.tutorsched

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private val bookingList = mutableListOf<Booking>()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        recyclerView = findViewById(R.id.recyclerViewMyBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty mutable list
        bookingAdapter = BookingAdapter(this, bookingList)
        recyclerView.adapter = bookingAdapter

        db = FirebaseFirestore.getInstance()
        fetchBookingsFromFirestore()
    }

    private fun fetchBookingsFromFirestore() {
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                bookingList.clear()
                for (document in result) {
                    val tutorName = document.getString("tutorName") ?: "Unknown Tutor"
                    val subject = document.getString("subject") ?: "General"
                    val date = document.getString("sessionDate") ?: ""
                    val time = document.getString("timeSlot") ?: ""
                    val status = document.getString("status") ?: "Pending"

                    // Map Firestore fields to your local Booking data class model
                    val booking = Booking(
                        tutorName = tutorName,
                        subject = subject,
                        date = date,
                        startTime = time,
                        endTime = "",
                        status = status
                    )
                    bookingList.add(booking)
                }
                // Notify your RecyclerView adapter that data has changed
                bookingAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load bookings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}