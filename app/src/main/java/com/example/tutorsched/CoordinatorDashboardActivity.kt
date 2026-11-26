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

class CoordinatorDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyBookings: TextView
    private lateinit var tvCoordinatorSummary: TextView
    private lateinit var bookingAdapter: BookingAdapter
    private val systemLogs = mutableListOf<Booking>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordinator_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val initialName = intent.getStringExtra("EXTRA_USER_NAME")?.trim().orEmpty()
        tvGreeting.text = "Hello, ${initialName.ifEmpty { "Coordinator" }}!"
        loadUserNameFromFirestore(tvGreeting)

        tvCoordinatorSummary = findViewById(R.id.tvCoordinatorSummary)
        tvEmptyBookings = findViewById(R.id.tvEmptyBookings)
        recyclerView = findViewById(R.id.recyclerViewCoordinatorLogs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(
            bookingList = systemLogs,
            showTutorActions = false,
            showLearnerActions = false
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

        fetchAllSystemBookings()
    }

    private fun loadUserNameFromFirestore(tvGreeting: TextView) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val savedName = doc.getString("fullName")?.trim().orEmpty()
                if (savedName.isNotEmpty()) {
                    tvGreeting.text = "Hello, $savedName!"
                }
            }
    }

    private fun updateSummaryCounts(allBookings: List<Booking>) {
        val total = allBookings.size
        val pending = allBookings.count { it.status.equals("Pending", ignoreCase = true) }
        val confirmed = allBookings.count { it.status.equals("Confirmed", ignoreCase = true) }
        val completed = allBookings.count { it.status.equals("Completed", ignoreCase = true) }
        tvCoordinatorSummary.text =
            "Total: $total | Pending: $pending | Confirmed: $confirmed | Completed: $completed"
    }

    private fun parseStartMinutesForSort(booking: Booking): Int {
        val firstPart = booking.startTime.substringBefore(" - ").trim()
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf.parse(firstPart) ?: return Int.MAX_VALUE
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (_: Exception) {
            Int.MAX_VALUE
        }
    }

    private fun updateEmptyState() {
        val isEmpty = systemLogs.isEmpty()
        tvEmptyBookings.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun fetchAllSystemBookings() {
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                val rawItems = mutableListOf<Booking>()
                val displayItems = mutableListOf<Booking>()

                for (document in result) {
                    val rawTutor = document.getString("tutorName")?.trim().orEmpty()
                    val tutorName = when {
                        rawTutor.isEmpty() || rawTutor == "1" || rawTutor == "tutor_1" -> "Alex Smith"
                        rawTutor == "2" || rawTutor == "tutor_2" -> "Jamie Reyes"
                        rawTutor == "3" || rawTutor == "tutor_3" -> "Chris Morales"
                        else -> rawTutor
                    }

                    val learnerName = document.getString("learnerName")?.trim().orEmpty().ifEmpty { "Learner" }
                    val rawSubject = document.getString("subject")?.trim().orEmpty()
                    val subject = if (rawSubject.isEmpty() ||
                        rawSubject.equals(tutorName, ignoreCase = true) ||
                        rawSubject.equals(learnerName, ignoreCase = true)
                    ) {
                        "Programming, CS101"
                    } else {
                        rawSubject
                    }

                    val date = document.getString("sessionDate") ?: ""
                    val time = document.getString("timeSlot") ?: ""
                    val status = document.getString("status") ?: "Pending"

                    rawItems.add(
                        Booking(
                            tutorName = tutorName,
                            subject = subject,
                            date = date,
                            startTime = time,
                            endTime = "",
                            status = status,
                            documentId = document.id
                        )
                    )

                    displayItems.add(
                        Booking(
                            tutorName = "$tutorName | Learner: $learnerName",
                            subject = subject,
                            date = date,
                            startTime = time,
                            endTime = "",
                            status = status,
                            documentId = document.id
                        )
                    )
                }

                updateSummaryCounts(rawItems)

                systemLogs.clear()
                systemLogs.addAll(displayItems)
                systemLogs.sortWith(compareBy<Booking> { it.date }.thenBy { parseStartMinutesForSort(it) })

                bookingAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
            .addOnFailureListener { exception ->
                updateEmptyState()
                Toast.makeText(this, "Failed to load coordinator logs: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}