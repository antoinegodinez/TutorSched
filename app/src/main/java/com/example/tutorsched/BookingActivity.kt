package com.example.tutorsched

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val tvTitle = findViewById<TextView>(R.id.tvSelectedTutor)
        val etDate = findViewById<EditText>(R.id.etBookingDate)
        val etStart = findViewById<EditText>(R.id.etStartTime)
        val etEnd = findViewById<EditText>(R.id.etEndTime)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitBooking)

        // get the tutor name passed from the previous activity
        val tutorName = intent.getStringExtra("EXTRA_TUTOR_NAME") ?: "Tutor"
        tvTitle.text = "Book Session with $tutorName"

        btnSubmit.setOnClickListener {
            val dateStr = etDate.text.toString().trim()
            val startStr = etStart.text.toString().trim()
            val endStr = etEnd.text.toString().trim()

            if (dateStr.isNotEmpty() && startStr.isNotEmpty() && endStr.isNotEmpty()) {
                Toast.makeText(this, "Booking submitted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all date and time fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}