package com.example.tutorsched

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutorsched.adapters.TutorAdapter
import com.example.tutorsched.models.Tutor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Mock data matching your Phase 1 wireframes
        val mockTutors = listOf(
            Tutor("1", "Alex", "Programming"),
            Tutor("2", "Jamie", "Database")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTutors)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = TutorAdapter(mockTutors) { selectedTutor ->
            // Action to pass data to the booking screen next
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("EXTRA_TUTOR_NAME", selectedTutor.name)
                putExtra("EXTRA_SUBJECT", selectedTutor.subject)
            }
            startActivity(intent)
        }
    }
}