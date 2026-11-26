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

class CoordinatorDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyBookings: TextView
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
        loadUserNameFromFirestore(tvGreeting, "Coordinator")

        tvEmptyBookings = findViewById(R.id.tvEmptyBookings)
        recyclerView = findViewById(R.id.recyclerViewCoordinatorLogs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(systemLogs)
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

    private fun loadUserNameFromFirestore(tvGreeting: TextView, defaultLabel: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val savedName = doc.getString("fullName")?.trim().orEmpty()
                if (savedName.isNotEmpty()) {
                    tvGreeting.text = "Hello, $savedName!"
                } else if (tvGreeting.text.isNullOrBlank()) {
                    tvGreeting.text = "Hello, $defaultLabel!"
                }
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
                systemLogs.clear()
                for (document in result) {
                    val tutorName = document.getString("tutorName") ?: "Tutor"
                    val subject = document.getString("subject") ?: "General"
                    val date = document.getString("sessionDate") ?: ""
                    val time = document.getString("timeSlot") ?: ""
                    val status = document.getString("status") ?: "Pending"

                    systemLogs.add(
                        Booking(
                            tutorName = "Tutor: $tutorName",
                            subject = subject,
                            date = date,
                            startTime = time,
                            endTime = "",
                            status = status
                        )
                    )
                }
                bookingAdapter.notifyDataSetChanged()
                updateEmptyState()
            }
            .addOnFailureListener { exception ->
                updateEmptyState()
                Toast.makeText(this, "Failed to load coordinator logs: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}