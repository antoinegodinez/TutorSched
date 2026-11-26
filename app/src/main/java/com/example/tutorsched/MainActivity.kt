package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.adapters.TutorAdapter
import com.example.tutorsched.models.Tutor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewTutors: RecyclerView
    private lateinit var tutorAdapter: TutorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val btnViewMyBookings = findViewById<Button>(R.id.btnViewMyBookings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        recyclerViewTutors = findViewById(R.id.recyclerViewTutors)
        recyclerViewTutors.layoutManager = LinearLayoutManager(this)

        val initialName = intent.getStringExtra("EXTRA_USER_NAME")?.trim().orEmpty()
        tvGreeting.text = "Hello, ${initialName.ifEmpty { "Student" }}!"
        loadUserNameFromFirestore(tvGreeting, "Student")

        val tutorList = listOf(
            Tutor("1", "Alex Smith", "Programming, CS101"),
            Tutor("2", "Jamie Reyes", "Database, SQL"),
            Tutor("3", "Chris Morales", "Data Structures")
        )

        tutorAdapter = TutorAdapter(this, tutorList)
        recyclerViewTutors.adapter = tutorAdapter

        btnViewMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
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
}