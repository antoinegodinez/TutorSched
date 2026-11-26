package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyBookings: TextView
    private lateinit var bookingAdapter: BookingAdapter
    private val bookingList = mutableListOf<Booking>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvEmptyBookings = findViewById(R.id.tvEmptyBookings)
        recyclerView = findViewById(R.id.recyclerViewMyBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(
            bookingList = bookingList,
            showLearnerActions = true,
            onCancelBooking = { booking -> cancelBooking(booking) },
            onDeleteBooking = { booking -> confirmDeleteBooking(booking) }
        )
        recyclerView.adapter = bookingAdapter

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        fetchLearnerBookings()
    }

    private fun cancelBooking(booking: Booking) {
        if (booking.documentId.isEmpty()) {
            Toast.makeText(this, "Missing booking document ID.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("bookings")
            .document(booking.documentId)
            .update(
                mapOf(
                    "status" to "Cancelled",
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Booking cancelled.", Toast.LENGTH_SHORT).show()
                fetchLearnerBookings()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to cancel booking: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmDeleteBooking(booking: Booking) {
        if (booking.documentId.isEmpty()) {
            Toast.makeText(this, "Missing booking document ID.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Booking")
            .setMessage("Are you sure you want to permanently delete this booking with ${booking.tutorName}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteBooking(booking)
            }
            .setNegativeButton("Keep", null)
            .show()
    }

    private fun deleteBooking(booking: Booking) {
        db.collection("bookings")
            .document(booking.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Booking deleted.", Toast.LENGTH_SHORT).show()
                fetchLearnerBookings()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete booking: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
        val isEmpty = bookingList.isEmpty()
        tvEmptyBookings.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun fetchLearnerBookings() {
        val currentUid = auth.currentUser?.uid.orEmpty()

        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                val allBookings = mutableListOf<Booking>()
                val ownBookings = mutableListOf<Booking>()

                for (document in result) {
                    val learnerId = document.getString("learnerId").orEmpty()
                    val rawTutor = document.getString("tutorName")?.trim().orEmpty()
                    val tutorName = when {
                        rawTutor.isEmpty() || rawTutor == "1" || rawTutor == "tutor_1" -> "Alex Smith"
                        rawTutor == "2" || rawTutor == "tutor_2" -> "Jamie Reyes"
                        rawTutor == "3" || rawTutor == "tutor_3" -> "Chris Morales"
                        else -> rawTutor
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
                        tutorName = tutorName,
                        subject = subject,
                        date = date,
                        startTime = time,
                        endTime = "",
                        status = status,
                        documentId = document.id
                    )
                    allBookings.add(item)

                    if (currentUid.isNotEmpty() && learnerId == currentUid) {
                        ownBookings.add(item)
                    }
                }

                bookingList.clear()
                if (currentUid.isNotEmpty() && ownBookings.isNotEmpty()) {
                    bookingList.addAll(ownBookings)
                } else {
                    bookingList.addAll(allBookings)
                }

                bookingList.sortWith(compareBy<Booking> { it.date }.thenBy { parseStartMinutesForSort(it) })

                bookingAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
            .addOnFailureListener { e ->
                updateEmptyState()
                Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}