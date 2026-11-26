package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.adapters.BookingAdapter
import com.example.tutorsched.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TutorScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyBookings: TextView
    private lateinit var bookingAdapter: BookingAdapter
    private val requestList = mutableListOf<Booking>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentTutorName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutor_schedule)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val initialName = intent.getStringExtra("EXTRA_USER_NAME")?.trim().orEmpty()
        currentTutorName = initialName
        tvGreeting.text = "Hello, ${initialName.ifEmpty { "Tutor" }}!"

        tvEmptyBookings = findViewById(R.id.tvEmptyBookings)
        recyclerView = findViewById(R.id.recyclerViewTutorSchedule)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(
            bookingList = requestList,
            showTutorActions = true,
            onStatusAction = { booking, newStatus ->
                handleStatusTransition(booking, newStatus)
            }
        )
        recyclerView.adapter = bookingAdapter

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        loadUserNameAndBookings(tvGreeting)
    }

    private fun loadUserNameAndBookings(tvGreeting: TextView) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            fetchTutorBookings()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val savedName = doc.getString("fullName")?.trim().orEmpty()
                if (savedName.isNotEmpty()) {
                    currentTutorName = savedName
                    tvGreeting.text = "Hello, $savedName!"
                } else if (tvGreeting.text.isNullOrBlank()) {
                    tvGreeting.text = "Hello, Tutor!"
                }
                fetchTutorBookings()
            }
            .addOnFailureListener {
                fetchTutorBookings()
            }
    }

    private fun handleStatusTransition(booking: Booking, newStatus: String) {
        if (booking.documentId.isEmpty()) {
            Toast.makeText(this, "Missing booking document ID.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newStatus.equals("Completed", ignoreCase = true) &&
            !booking.status.equals("Confirmed", ignoreCase = true)
        ) {
            Toast.makeText(this, "Only confirmed sessions can be marked Completed.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newStatus.equals("Confirmed", ignoreCase = true)) {
            db.collection("bookings")
                .whereEqualTo("sessionDate", booking.date)
                .get()
                .addOnSuccessListener { docs ->
                    val (targetStart, targetEnd) = parseRangeFromTimeSlot(booking.startTime)
                    val targetTutorPrefix = booking.tutorName.substringBefore(" (Learner:").trim()

                    var hasOverlap = false
                    for (doc in docs) {
                        if (doc.id == booking.documentId) continue
                        val status = doc.getString("status") ?: "Pending"
                        if (!status.equals("Confirmed", ignoreCase = true)) continue

                        val docTutor = doc.getString("tutorName") ?: ""
                        if (targetTutorPrefix.isNotEmpty() &&
                            !docTutor.equals(targetTutorPrefix, ignoreCase = true) &&
                            !docTutor.startsWith(targetTutorPrefix, ignoreCase = true)
                        ) continue

                        val (exStart, exEnd) = extractMinutesRange(
                            startMinLong = doc.getLong("startMinutes"),
                            endMinLong = doc.getLong("endMinutes"),
                            timeSlot = doc.getString("timeSlot").orEmpty()
                        )

                        if (targetStart != null && targetEnd != null && exStart != null && exEnd != null) {
                            if (targetStart < exEnd && targetEnd > exStart) {
                                hasOverlap = true
                                break
                            }
                        } else if (doc.getString("timeSlot").equals(booking.startTime, ignoreCase = true)) {
                            hasOverlap = true
                            break
                        }
                    }

                    if (hasOverlap) {
                        Toast.makeText(
                            this,
                            "Cannot confirm: overlaps with another confirmed session on ${booking.date}.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        applyStatusUpdate(booking.documentId, newStatus)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Conflict check failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            applyStatusUpdate(booking.documentId, newStatus)
        }
    }

    private fun applyStatusUpdate(documentId: String, newStatus: String) {
        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        if (newStatus.equals("Completed", ignoreCase = true)) {
            updates["completedAt"] = com.google.firebase.Timestamp.now()
        }

        db.collection("bookings")
            .document(documentId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking marked as $newStatus.", Toast.LENGTH_SHORT).show()
                fetchTutorBookings()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_LONG).show()
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
        return parseRangeFromTimeSlot(timeSlot)
    }

    private fun parseRangeFromTimeSlot(timeSlot: String): Pair<Int?, Int?> {
        val parts = timeSlot.split(" - ")
        if (parts.size == 2) {
            val s = parse12HourToMinutes(parts[0].trim())
            val e = parse12HourToMinutes(parts[1].trim())
            if (s != null && e != null) return s to e
        }
        return null to null
    }

    private fun parseStartMinutesForSort(booking: Booking): Int {
        val firstPart = booking.startTime.substringBefore(" - ").trim()
        return parse12HourToMinutes(firstPart) ?: Int.MAX_VALUE
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

    private fun updateEmptyState() {
        val isEmpty = requestList.isEmpty()
        tvEmptyBookings.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun fetchTutorBookings() {
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                val allBookings = mutableListOf<Booking>()
                val filteredBookings = mutableListOf<Booking>()

                for (document in result) {
                    val rawTutor = document.getString("tutorName")?.trim().orEmpty()
                    val tutorName = when {
                        rawTutor.isEmpty() || rawTutor == "1" || rawTutor == "tutor_1" -> "Alex Smith"
                        rawTutor == "2" || rawTutor == "tutor_2" -> "Jamie Reyes"
                        rawTutor == "3" || rawTutor == "tutor_3" -> "Chris Morales"
                        else -> rawTutor
                    }

                    val learnerName = document.getString("learnerName")?.trim().orEmpty()
                    val displayTutor = if (learnerName.isNotEmpty()) {
                        "$tutorName (Learner: $learnerName)"
                    } else {
                        tutorName
                    }

                    val rawSubject = document.getString("subject")?.trim().orEmpty()
                    val subject = if (rawSubject.isEmpty() || rawSubject.equals(tutorName, ignoreCase = true)) {
                        "Programming, CS101"
                    } else {
                        rawSubject
                    }

                    val date = document.getString("sessionDate") ?: ""
                    val time = document.getString("timeSlot") ?: ""
                    val status = document.getString("status") ?: "Pending"

                    val item = Booking(
                        tutorName = displayTutor,
                        subject = subject,
                        date = date,
                        startTime = time,
                        endTime = "",
                        status = status,
                        documentId = document.id
                    )
                    allBookings.add(item)

                    if (currentTutorName.isNotEmpty() &&
                        tutorName.contains(currentTutorName, ignoreCase = true)
                    ) {
                        filteredBookings.add(item)
                    }
                }

                requestList.clear()
                if (currentTutorName.isNotEmpty() && filteredBookings.isNotEmpty()) {
                    requestList.addAll(filteredBookings)
                } else {
                    requestList.addAll(allBookings)
                }

                requestList.sortWith(compareBy<Booking> { it.date }.thenBy { parseStartMinutesForSort(it) })

                bookingAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
            .addOnFailureListener { exception ->
                updateEmptyState()
                Toast.makeText(this, "Failed to load tutor schedule: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}