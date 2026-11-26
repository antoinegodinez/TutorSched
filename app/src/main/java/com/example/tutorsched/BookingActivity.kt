package com.example.tutorsched

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
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
        val tutorSubject = intent.getStringExtra("EXTRA_TUTOR_SUBJECTS")
            ?: intent.getStringExtra("EXTRA_TUTOR_SUBJECT")
            ?: "General Programming"
        tvTitle.text = "Book Session with $tutorName"

        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedMonth = String.format("%02d", selectedMonth + 1)
                    val formattedDay = String.format("%02d", selectedDay)
                    etDate.setText("$selectedYear-$formattedMonth-$formattedDay")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etStart.isFocusable = false
        etStart.isClickable = true
        etStart.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    etStart.setText(formatTime(selectedHour, selectedMinute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        etEnd.isFocusable = false
        etEnd.isClickable = true
        etEnd.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    etEnd.setText(formatTime(selectedHour, selectedMinute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        btnSubmit.setOnClickListener {
            val dateStr = etDate.text.toString().trim()
            val startStr = etStart.text.toString().trim()
            val endStr = etEnd.text.toString().trim()

            if (dateStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all date and time fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            val currentUser = auth.currentUser
            val learnerId = currentUser?.uid ?: "anonymous_learner"
            val emailFallback = currentUser?.email?.substringBefore("@").orEmpty()

            if (currentUser != null) {
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { doc ->
                        val savedName = doc.getString("fullName")?.trim().orEmpty()
                        val learnerName = when {
                            savedName.isNotEmpty() -> savedName
                            emailFallback.isNotEmpty() -> emailFallback
                            else -> "Learner"
                        }
                        saveBookingToFirestore(
                            btnSubmit = btnSubmit,
                            learnerId = learnerId,
                            learnerName = learnerName,
                            tutorName = tutorName,
                            subject = tutorSubject,
                            dateStr = dateStr,
                            timeSlot = "$startStr - $endStr"
                        )
                    }
                    .addOnFailureListener {
                        val learnerName = emailFallback.ifEmpty { "Learner" }
                        saveBookingToFirestore(
                            btnSubmit = btnSubmit,
                            learnerId = learnerId,
                            learnerName = learnerName,
                            tutorName = tutorName,
                            subject = tutorSubject,
                            dateStr = dateStr,
                            timeSlot = "$startStr - $endStr"
                        )
                    }
            } else {
                saveBookingToFirestore(
                    btnSubmit = btnSubmit,
                    learnerId = learnerId,
                    learnerName = "Learner",
                    tutorName = tutorName,
                    subject = tutorSubject,
                    dateStr = dateStr,
                    timeSlot = "$startStr - $endStr"
                )
            }
        }
    }

    private fun saveBookingToFirestore(
        btnSubmit: Button,
        learnerId: String,
        learnerName: String,
        tutorName: String,
        subject: String,
        dateStr: String,
        timeSlot: String
    ) {
        val bookingMap = hashMapOf(
            "learnerId" to learnerId,
            "learnerName" to learnerName,
            "tutorName" to tutorName,
            "subject" to subject,
            "sessionDate" to dateStr,
            "timeSlot" to timeSlot,
            "status" to "Pending",
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("bookings")
            .add(bookingMap)
            .addOnSuccessListener {
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Booking saved by $learnerName!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MyBookingsActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Error saving booking: ${e.message}", Toast.LENGTH_LONG).show()
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