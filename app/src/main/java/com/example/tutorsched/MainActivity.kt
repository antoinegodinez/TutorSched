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
    private val tutorList = mutableListOf<Tutor>()

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

        loadTutorsFromFirestore()
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

    private fun loadTutorsFromFirestore() {
        db.collection("tutorProfiles").get()
            .addOnSuccessListener { profileDocs ->
                db.collection("users").whereEqualTo("role", "tutor").get()
                    .addOnSuccessListener { userDocs ->
                        val userNamesById = mutableMapOf<String, String>()
                        for (uDoc in userDocs) {
                            val name = uDoc.getString("fullName")?.trim().orEmpty()
                            if (name.isNotEmpty()) {
                                userNamesById[uDoc.id] = name
                            }
                        }

                        val loadedTutors = mutableListOf<Tutor>()
                        for (pDoc in profileDocs) {
                            val isAvailable = pDoc.getBoolean("isAvailable") ?: true
                            if (!isAvailable) continue

                            val tutorId = pDoc.getString("tutorId")?.trim().orEmpty().ifEmpty { pDoc.id }
                            val rawName = pDoc.getString("fullName")?.trim()
                                ?: pDoc.getString("tutorName")?.trim()
                                ?: userNamesById[tutorId]
                                ?: "Alex Smith"

                            val subjectField = when (val subjVal = pDoc.get("subjects") ?: pDoc.get("subject")) {
                                is List<*> -> subjVal.filterNotNull()
                                    .map { it.toString().trim() }
                                    .filter { it.isNotEmpty() }
                                    .joinToString(", ")
                                is String -> subjVal.trim()
                                else -> ""
                            }
                            val cleanSubject = if (subjectField.isEmpty() || subjectField.equals(rawName, ignoreCase = true)) {
                                "Programming, CS101"
                            } else {
                                subjectField
                            }

                            loadedTutors.add(Tutor(rawName, cleanSubject, "Available", tutorId))
                        }

                        tutorList.clear()
                        if (loadedTutors.isNotEmpty()) {
                            tutorList.addAll(loadedTutors)
                        } else {
                            tutorList.addAll(
                                listOf(
                                    Tutor("Alex Smith", "Programming, CS101", "Available", "tutor_1"),
                                    Tutor("Jamie Reyes", "Database, SQL", "Available", "tutor_2"),
                                    Tutor("Chris Morales", "Data Structures", "Available", "tutor_3")
                                )
                            )
                        }
                        tutorAdapter.notifyDataSetChanged()
                    }
            }
    }
}