package com.example.tutorsched

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var bookingList: List<Booking>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMyBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample local mock data to display learner bookings for Phase 1
        bookingList = listOf(
            Booking(
                tutorName = "Alex Smith",
                subject = "Programming, CS101",
                date = "2026-11-10",
                startTime = "10:00 AM",
                endTime = "11:00 AM",
                status = "Pending"
            ),
            Booking(
                tutorName = "Jamie Reyes",
                subject = "Database, SQL",
                date = "2026-11-12",
                startTime = "01:00 PM",
                endTime = "02:00 PM",
                status = "Confirmed"
            )
        )

        // Set up the adapter with the mock list
        bookingAdapter = BookingAdapter(this, bookingList)
        recyclerView.adapter = bookingAdapter
    }
}