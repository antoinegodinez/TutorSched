package com.example.tutorsched

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class BookingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val tvTitle = findViewById<TextView>(R.id.tvSelectedTutor)
        val etDate = findViewById<EditText>(R.id.etBookingDate)
        val etStart = findViewById<EditText>(R.id.etStartTime)
        val etEnd = findViewById<EditText>(R.id.etEndTime)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitBooking)

        val tutorName = intent.getStringExtra("EXTRA_TUTOR_NAME") ?: "Tutor"
        val tutorSubject = intent.getStringExtra("EXTRA_TUTOR_SUBJECT") ?: "General Programming"
        tvTitle.text = "Book Session with $tutorName"

        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                etDate.setText("$selectedYear-$formattedMonth-$formattedDay")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        etStart.isFocusable = false
        etStart.isClickable = true
        etStart.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                etStart.setText(formatTime(selectedHour, selectedMinute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        etEnd.isFocusable = false
        etEnd.isClickable = true
        etEnd.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                etEnd.setText(formatTime(selectedHour, selectedMinute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        btnSubmit.setOnClickListener {
            val dateStr = etDate.text.toString().trim()
            val startStr = etStart.text.toString().trim()
            val endStr = etEnd.text.toString().trim()
            val learnerId = auth.currentUser?.uid ?: "anonymous_learner"

            if (dateStr.isNotEmpty() && startStr.isNotEmpty() && endStr.isNotEmpty()) {
                val bookingMap = hashMapOf(
                    "learnerId" to learnerId,
                    "tutorName" to tutorName,
                    "subject" to tutorSubject,
                    "sessionDate" to dateStr,
                    "timeSlot" to "$startStr - $endStr",
                    "status" to "Pending",
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                db.collection("bookings")
                    .add(bookingMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Booking saved to Firestore!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MyBookingsActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving booking: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all date and time fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val formattedHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$formattedHour:${String.format("%02d", minute)} $amPm"
    }
}