package com.example.tutorsched

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

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

        // make date field open a calendar picker dialog
        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                etDate.setText("$selectedYear-$formattedMonth-$formattedDay")
            }, year, month, day).show()
        }

        // make start time field open a time picker dialog
        etStart.isFocusable = false
        etStart.isClickable = true
        etStart.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedTime = formatTime(selectedHour, selectedMinute)
                etStart.setText(formattedTime)
            }, hour, minute, false).show() // false for 12-hour AM/PM format
        }

        // make end time field open a time picker dialog
        etEnd.isFocusable = false
        etEnd.isClickable = true
        etEnd.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedTime = formatTime(selectedHour, selectedMinute)
                etEnd.setText(formattedTime)
            }, hour, minute, false).show()
        }

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

    // helper function to format 24-hour time into 12-hour AM/PM format
    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val formattedHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val formattedMinute = String.format("%02d", minute)
        return "$formattedHour:$formattedMinute $amPm"
    }
}