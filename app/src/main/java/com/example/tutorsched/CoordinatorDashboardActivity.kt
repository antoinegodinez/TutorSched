package com.example.tutorsched

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CoordinatorDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var allBookingsList: List<Booking>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordinator_dashboard)

        recyclerView = findViewById(R.id.recyclerViewCoordinator)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // All system bookings mock data
        allBookingsList = listOf(
            Booking(
                tutorName = "Alex Smith",
                subject = "Programming, CS101",
                date = "2026-11-15",
                startTime = "10:00 AM",
                endTime = "11:00 AM",
                status = "Pending"
            ),
            Booking(
                tutorName = "Jamie Reyes",
                subject = "Database, SQL",
                date = "2026-11-16",
                startTime = "02:00 PM",
                endTime = "03:00 PM",
                status = "Confirmed"
            )
        )

        bookingAdapter = BookingAdapter(this, allBookingsList)
        recyclerView.adapter = bookingAdapter
    }
}