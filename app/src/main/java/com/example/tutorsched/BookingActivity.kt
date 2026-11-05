package com.example.tutorsched

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        // Retrieve data passed from MainActivity
        val tutorName = intent.getStringExtra("EXTRA_TUTOR_NAME")
        val subject = intent.getStringExtra("EXTRA_SUBJECT")

        val inputDate = findViewById<EditText>(R.id.inputDate)
        val inputTime = findViewById<EditText>(R.id.inputTime)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitBooking)

        // 1. Open Calendar Date Picker on click
        inputDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                inputDate.setText(formattedDate)
            }, year, month, day).show()
        }

        // 2. Open Clock Time Picker on click
        inputTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val hourFormatted = when {
                    selectedHour == 0 -> 12
                    selectedHour > 12 -> selectedHour - 12
                    else -> selectedHour
                }
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val minuteFormatted = String.format("%02d", selectedMinute)

                val formattedTime = "$hourFormatted:$minuteFormatted $amPm"
                inputTime.setText(formattedTime)
            }, hour, minute, false).show()
        }

        // 3. Submit Button validation & feedback
        btnSubmit.setOnClickListener {
            val date = inputDate.text.toString()
            val time = inputTime.text.toString()

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please select both a date and a time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Successfully booked $tutorName ($subject) for $date at $time!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}