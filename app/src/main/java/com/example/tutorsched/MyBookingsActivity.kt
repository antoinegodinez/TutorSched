package com.example.tutorsched

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.adapters.BookingAdapter
import com.example.tutorsched.models.Booking
import com.google.firebase.firestore.FirebaseFirestore

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyBookings: TextView
    private lateinit var bookingAdapter: BookingAdapter
    private val bookingList = mutableListOf<Booking>()
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        tvEmptyBookings = findViewById(R.id.tvEmptyBookings)
        recyclerView = findViewById(R.id.recyclerViewMyBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(bookingList)
        recyclerView.adapter = bookingAdapter

        db = FirebaseFirestore.getInstance()
        loadBookings()
    }

    private fun updateEmptyState() {
        val isEmpty = bookingList.isEmpty()
        tvEmptyBookings.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun loadBookings() {
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                bookingList.clear()
                for (doc in result) {
                    val tutorName = doc.getString("tutorName") ?: "Unknown Tutor"
                    val subject = doc.getString("subject") ?: "General"
                    val date = doc.getString("sessionDate") ?: ""
                    val time = doc.getString("timeSlot") ?: ""
                    val status = doc.getString("status") ?: "Pending"

                    bookingList.add(
                        Booking(
                            tutorName = tutorName,
                            subject = subject,
                            date = date,
                            startTime = time,
                            endTime = "",
                            status = status
                        )
                    )
                }
                bookingAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
            .addOnFailureListener { e ->
                updateEmptyState()
                Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}