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

        recyclerView = findViewById(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // mock data to test booking status list
        bookingList = listOf(
            Booking("Alex Smith", "Programming", "2026-10-30", "10:00 AM", "11:00 AM", "Pending"),
            Booking("Jamie Reyes", "Database", "2026-10-31", "01:00 PM", "02:00 PM", "Confirmed")
        )

        bookingAdapter = BookingAdapter(this, bookingList)
        recyclerView.adapter = bookingAdapter
    }
}