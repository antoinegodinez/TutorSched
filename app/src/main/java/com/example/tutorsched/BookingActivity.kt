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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var startMinutes: Int = -1
    private var endMinutes: Int = -1

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

        val tutorId = intent.getStringExtra("EXTRA_TUTOR_ID")?.trim().orEmpty()
        val tutorName = intent.getStringExtra("EXTRA_TUTOR_NAME")?.trim().orEmpty().ifEmpty { "Alex Smith" }
        val rawSubject = (intent.getStringExtra("EXTRA_TUTOR_SUBJECTS")
            ?: intent.getStringExtra("EXTRA_TUTOR_SUBJECT")
            ?: "Programming, CS101").trim()
        val tutorSubject = if (rawSubject.isEmpty() || rawSubject.equals(tutorName, ignoreCase = true)) {
            "Programming, CS101"
        } else {
            rawSubject
        }

        tvTitle.text = "Book $tutorSubject with$tutorName"

        etDate.isFocusable = false
        etDate.isClickable = true
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val picker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedMonth = String.format(Locale.US, "%02d", selectedMonth + 1)
                    val formattedDay = String.format(Locale.US, "%02d", selectedDay)
                    etDate.setText("$selectedYear-$formattedMonth-$formattedDay")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            picker.datePicker.minDate = System.currentTimeMillis() - 1000
            picker.show()
        }

        etStart.isFocusable = false
        etStart.isClickable = true
        etStart.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    startMinutes = selectedHour * 60 + selectedMinute
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
                    endMinutes = selectedHour * 60 + selectedMinute
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
                Toast.makeText(this, "Please select a date, start time, and end time.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nowCalendar = Calendar.getInstance()
            val todayStr = formatDate(nowCalendar)
            val nowMinutes = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE)

            if (dateStr < todayStr) {
                Toast.makeText(this, "You cannot book a session on a past date.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dateStr == todayStr && startMinutes != -1 && startMinutes < nowMinutes) {
                Toast.makeText(this, "That time has already passed today. Choose a later start time.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (startMinutes != -1 && endMinutes != -1 && endMinutes <= startMinutes) {
                Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            val timeSlot = "$startStr -$endStr"

            db.collection("bookings")
                .whereEqualTo("sessionDate", dateStr)
                .get()
                .addOnSuccessListener { existingDocs ->
                    var conflictSlot: String? = null
                    for (doc in existingDocs) {
                        val existingStatus = doc.getString("status") ?: "Pending"
                        if (!existingStatus.equals("Confirmed", ignoreCase = true)) continue

                        val existingTutorId = doc.getString("tutorId").orEmpty()
                        val existingTutor = doc.getString("tutorName").orEmpty()
                        val sameTutor = (tutorId.isNotEmpty() && existingTutorId == tutorId) ||
                                existingTutor.equals(tutorName, ignoreCase = true) ||
                                existingTutor.startsWith(tutorName, ignoreCase = true)
                        if (!sameTutor) continue

                        val (exStart, exEnd) = extractMinutesRange(
                            startMinLong = doc.getLong("startMinutes"),
                            endMinLong = doc.getLong("endMinutes"),
                            timeSlot = doc.getString("timeSlot").orEmpty()
                        )

                        if (exStart != null && exEnd != null && startMinutes != -1 && endMinutes != -1) {
                            if (startMinutes < exEnd && endMinutes > exStart) {
                                conflictSlot = doc.getString("timeSlot") ?: timeSlot
                                break
                            }
                        } else if (doc.getString("timeSlot").equals(timeSlot, ignoreCase = true)) {
                            conflictSlot = timeSlot
                            break
                        }
                    }

                    if (conflictSlot != null) {
                        btnSubmit.isEnabled = true
                        Toast.makeText(
                            this,
                            "Time conflict with confirmed session ($conflictSlot) for $tutorName on$dateStr.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        resolveLearnerAndSave(
                            btnSubmit = btnSubmit,
                            tutorId = tutorId,
                            tutorName = tutorName,
                            subject = tutorSubject,
                            dateStr = dateStr,
                            startStr = startStr,
                            endStr = endStr,
                            timeSlot = timeSlot
                        )
                    }
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Schedule check failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun resolveLearnerAndSave(
        btnSubmit: Button,
        tutorId: String,
        tutorName: String,
        subject: String,
        dateStr: String,
        startStr: String,
        endStr: String,
        timeSlot: String
    ) {
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
                        btnSubmit, learnerId, learnerName, tutorId, tutorName, subject, dateStr, startStr, endStr, timeSlot
                    )
                }
                .addOnFailureListener {
                    saveBookingToFirestore(
                        btnSubmit, learnerId, emailFallback.ifEmpty { "Learner" }, tutorId, tutorName, subject, dateStr, startStr, endStr, timeSlot
                    )
                }
        } else {
            saveBookingToFirestore(
                btnSubmit, learnerId, "Learner", tutorId, tutorName, subject, dateStr, startStr, endStr, timeSlot
            )
        }
    }

    private fun saveBookingToFirestore(
        btnSubmit: Button,
        learnerId: String,
        learnerName: String,
        tutorId: String,
        tutorName: String,
        subject: String,
        dateStr: String,
        startStr: String,
        endStr: String,
        timeSlot: String
    ) {
        val bookingMap = hashMapOf(
            "learnerId" to learnerId,
            "learnerName" to learnerName,
            "tutorId" to tutorId,
            "tutorName" to tutorName,
            "subject" to subject,
            "sessionDate" to dateStr,
            "startTime" to startStr,
            "endTime" to endStr,
            "startMinutes" to startMinutes,
            "endMinutes" to endMinutes,
            "timeSlot" to timeSlot,
            "status" to "Pending",
            "createdAt" to com.google.firebase.Timestamp.now(),
            "updatedAt" to com.google.firebase.Timestamp.now()
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

    private fun extractMinutesRange(
        startMinLong: Long?,
        endMinLong: Long?,
        timeSlot: String
    ): Pair<Int?, Int?> {
        if (startMinLong != null && endMinLong != null && startMinLong >= 0 && endMinLong > startMinLong) {
            return startMinLong.toInt() to endMinLong.toInt()
        }
        val parts = timeSlot.split(" - ")
        if (parts.size == 2) {
            val s = parse12HourToMinutes(parts[0].trim())
            val e = parse12HourToMinutes(parts[1].trim())
            if (s != null && e != null) return s to e
        }
        return null to null
    }

    private fun parse12HourToMinutes(timeText: String): Int? {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf.parse(timeText) ?: return null
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (_: Exception) {
            null
        }
    }

    private fun formatDate(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = String.format(Locale.US, "%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format(Locale.US, "%02d", calendar.get(Calendar.DAY_OF_MONTH))
        return "$year-$month-$day"
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val formattedHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$formattedHour:${String.format(Locale.US, "%02d", minute)} $amPm"
    }
}