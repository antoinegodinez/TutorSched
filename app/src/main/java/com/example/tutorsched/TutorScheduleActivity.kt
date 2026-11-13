package com.example.tutorsched

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TutorScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var requestList: List<Booking>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_schedule)

        recyclerView = findViewById(R.id.recyclerViewTutorSchedule)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Mock pending requests assigned to this tutor
        requestList = listOf(
            Booking(
                tutorName = "Alex Smith (You)",
                subject = "Programming, CS101",
                date = "2026-11-15",
                startTime = "10:00 AM",
                endTime = "11:00 AM",
                status = "Pending Approval"
            )
        )

        bookingAdapter = BookingAdapter(this, requestList)
        recyclerView.adapter = bookingAdapter
    }
}